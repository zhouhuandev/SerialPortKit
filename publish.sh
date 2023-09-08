#!/usr/bin/env bash
module=$1

# 检查输入的 module 不能为空
if [ -z "$module" ]; then
  echo "Usage: ./publish.sh <module_name>"
  exit 1
fi

# 列出已有的参数名称
valid_params=("local" "no-build" "version" "snapshot")
# 校验 module 参数是否与已有的参数名称冲突
for valid_param in "${valid_params[@]}"; do
  if [ "$module" == "--$valid_param" ]; then
    echo "Error: The 'module' parameter cannot have the same name as a valid parameter: $module"
    exit 1
  fi
done

# opt_build != 1 则不进行构建
opt_build=1
# opt_local != 0 则推送至远端Maven
opt_local=0
# 推送版本 0.0.0 格式
opt_version=''
# true 发布快照版；false 发布生产版本
opt_snapshot=''

while [[ $# -gt 0 ]]; do
  case $1 in
  --local)
    opt_local=1
    ;;
  --no-build)
    opt_build=0
    ;;
  --v | --version)
    shift
    opt_version=$1
    ;;
  --snapshot)
    shift
    case $1 in
    true | false)
      opt_snapshot=$1
      ;;
    *)
      echo "Error: --snapshot parameter must be 'true' or 'false'."
      exit 1
      ;;
    esac
    ;;
  *) ;;

  esac
  shift
done

echo "prepare to publish module: $module"

sed -i -c "/release.offline=/ s/=.*/=false/" "$module/gradle.properties"

if [ -n "$opt_version" ]; then
  echo "Version: $opt_version"
  sed -i -c "/lib.version=/ s/=.*/=${opt_version}/" "$module/gradle.properties"
fi

if [ -n "$opt_snapshot" ]; then
  echo "Snapshot: $opt_snapshot"
  sed -i -c "/lib.snapshot=/ s/=.*/=${opt_snapshot}/" "$module/gradle.properties"
fi

if [ "$opt_build" -eq 1 ]; then
  ./gradlew "$module:clean" "$module:build"
fi

if [ "$opt_local" -eq 1 ]; then
  echo 'publish to maven local'
  ./gradlew "$module:publishToMavenLocal"
else
  echo 'publish to maven repository'
  ./gradlew "$module:publish"
fi

sed -i -c "/release.offline=/ s/=.*/=true/" "$module/gradle.properties"
rm "$module/gradle.properties-c"
