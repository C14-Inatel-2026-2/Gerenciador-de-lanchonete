# Imagem do Jenkins
# Versão LTS fixa para o build ser reproduzível. Java 21 porque o Jenkins não
# suporta mais o Java 17; o backend continua em Java 17 (o JDK 21 compila para 17).
FROM jenkins/jenkins:2.580.1-lts-jdk21

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

