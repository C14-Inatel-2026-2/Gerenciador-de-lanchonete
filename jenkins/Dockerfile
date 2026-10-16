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
USER jenkins

# Plugins pré-instalados: ninguém precisa instalar nada pela interface
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt