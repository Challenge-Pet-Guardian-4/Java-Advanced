# ============================================================
# Stage 1 — BUILD: compila o projeto com Gradle
# ============================================================
FROM gradle:jdk17 AS build

WORKDIR /app

# Copia apenas os arquivos de dependências primeiro (melhora cache de layers)
COPY build.gradle settings.gradle ./
COPY gradle/ gradle/
RUN gradle dependencies --no-daemon || true

# Copia o restante do código-fonte
COPY src/ src/

# Garante que as chaves RSA para JWT existam antes de empacotar o JAR (sem expor no GitHub)
RUN mkdir -p src/main/resources/keys && \
    if [ ! -f src/main/resources/keys/private_key.pem ]; then \
        apt-get update && apt-get install -y openssl && \
        openssl genpkey -algorithm RSA -out src/main/resources/keys/private_key.pem -pkeyopt rsa_keygen_bits:2048 && \
        openssl rsa -pubout -in src/main/resources/keys/private_key.pem -out src/main/resources/keys/public_key.pem && \
        rm -rf /var/lib/apt/lists/*; \
    fi

RUN gradle bootJar --no-daemon -x test

# ============================================================
# Stage 2 — RUNTIME: imagem mínima de produção
# ============================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Instala tzdata e configura fuso horário oficial de Brasília (America/Sao_Paulo)
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/America/Sao_Paulo /etc/localtime && \
    echo "America/Sao_Paulo" > /etc/timezone

# Cria usuário e grupo sem privilégios administrativos (non-root)
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copia o JAR gerado no estágio de build
COPY --from=build /app/build/libs/pet-guardian-0.0.1-SNAPSHOT.jar app.jar

# Define permissões corretas para o usuário não privilegiado
RUN chown -R appuser:appgroup /app

# Define timezone padrão no ambiente Linux
ENV TZ=America/Sao_Paulo

# Executa como usuário sem privilégios administrativos
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=America/Sao_Paulo", "-jar", "app.jar"]

