#!/usr/bin/env bash
module=$1

opt_build=1
opt_local=0

for arg in "$@"
do
  if [[ $arg == --* ]];
  then
    tmp=${arg#*--}
    case $tmp in
      local)
        opt_local=1
        ;;
      no-build)
        opt_build=0
        ;;
      *)
        ;;
    esac
  fi
done

echo "prepare to publish module: $module"

sed -i -c "/release.offline=/ s/=.*/=false/" $module/gradle.properties

if [ $opt_build == 1 ]; then
  ./gradlew $module:clean $module:build
fi

if [ $opt_local == 1 ]; then
  echo 'publish to maven local'
  ./gradlew $module:publishToMavenLocal
else
  echo 'publish to maven repository'
  ./gradlew $module:publish
fi

sed -i -c "/release.offline=/ s/=.*/=true/" $module/gradle.properties
rm $module/gradle.properties-c
