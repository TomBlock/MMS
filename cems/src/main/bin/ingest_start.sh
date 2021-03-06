#!/bin/bash

# source environment definitions
. ${PM_EXE_DIR}/${MMS_ENV_NAME}

sensor=$1
start_date=$2
end_date=$3
data_version=$4
config_dir=$5

task="ingest"
jobname="${task}-${sensor}-${data_version}-${start_date}-${end_date}"
echo ${jobname}

command="${task}_run.sh ${sensor} ${start_date} ${end_date} ${data_version} ${config_dir}"

echo "`date -u +%Y%m%d-%H%M%S` submitting job '${jobname}'"

if [ -z ${jobs} ]; then
    submit_job ${jobname} ${command}
fi