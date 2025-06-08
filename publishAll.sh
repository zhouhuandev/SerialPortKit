#!/usr/bin/env bash
opt_version='1.0.6.6'
opt_snapshot='false'

while [[ $# -gt 0 ]]; do
  case $1 in
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

./publish.sh serial-port-kit-core --v "${opt_version}" --snapshot "${opt_snapshot}"
./publish.sh serial-port-kit-manage --v "${opt_version}" --snapshot "${opt_snapshot}"
