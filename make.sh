#!/usr/bin/env bash
set -euo pipefail
IFS=$'\t\n'

IMAGE=hello
CONTAINER=hello
PORT=8080

cmd_dbuild() {
    docker build -t ${IMAGE} .
}

cmd_drm() {
    docker rm -fv ${CONTAINER} ||  true
}

cmd_drun() {
    cmd_drm
    docker run -dt --name ${CONTAINER} \
        -p ${PORT}:8080 \
        ${IMAGE}
}

cmd_dshell() {
    docker exec -it ${CONTAINER} bash
}

cmd_dlogs() {
    docker logs -f ${CONTAINER}
}

cmd_generate-deploy-key() {
    ssh-keygen -t rsa -b 4096 -C deploy-key -f ./deploy-key.id_rsa -N ""
}

# Shortcuts
cmd_b() { cmd_dbuild; }
cmd_r() { cmd_drun; }
cmd_l() { cmd_dlogs; }
cmd_s() { cmd_dshell; }
cmd_br() { cmd_dbuild; cmd_drun; }
cmd_brl() { cmd_br; cmd_dlogs; }
cmd_brs() { cmd_br; cmd_dshell; }

# Print all defined cmd_
cmd_help() {
    compgen -A function cmd_
}

# Run multiple commands without args
cmd_mm() {
    for cmd in "$@"; do
        cmd_$cmd
    done
}

if [[ $# -eq 0 ]]; then
    echo Please provide a subcommand
    exit 1
fi

SUBCOMMAND=$1
shift

# Enable verbose mode
set -x
# Run the subcommand
cmd_${SUBCOMMAND} $@

