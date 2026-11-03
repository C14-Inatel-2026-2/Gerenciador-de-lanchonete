# Imagem do Jenkins
# Parte da imagem oficial com Java 17 (mesma versão do backend).
FROM jenkins/jenkins:lts-jdk17

# Node 22 para o stage de Frontend (npm ci, lint, testes, build do Vite)
USER root
RUN apt-get update \
    && apt-get install -y --no-install-recommends ca-certificates curl gnupg \
    && curl -fsSL https://deb.nodesource.com/setup_22.x | bash - \
    && apt-get install -y --no-install-recommends nodejs \
    && rm -rf /var/lib/apt/lists/*

# Docker CLI para o stage "Docker Build Backend".
# O Jenkins usa o Docker do próprio computador (socket montado no docker-compose.yml).
COPY --from=docker:27.5.1-cli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=docker:27.5.1-cli /usr/local/libexec/docker/cli-plugins/docker-buildx /usr/local/libexec/docker/cli-plugins/docker-buildx
USER jenkins

# Plugins pré-instalados: ninguém precisa instalar nada pela interface
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt