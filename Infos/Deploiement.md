# Pour se connecter au serveur VPS OVHcloud

ssh ubuntu@51.254.219.63 
key fourni par mail par OVH : VSHUQ7v333wF
modification du mot de passe : OVHcloud1*


sudo apt update && sudo apt upgrade -y && sudo apt install -y docker.io docker-compose-v2 git && sudo usermod -aG docker ubuntu

newgrp docker

sudo mkdir -p /var/www/QuadGPT
sudo chown -R ubuntu:ubuntu /var/www/QuadGPT
cd /var/www/QuadGPT

nano .env
Collez-y vos variables DB_NAME, DB_USER, DB_PASSWORD, puis enregistrez avec Ctrl + O, Entrée, et quittez avec Ctrl + X


ssh-keygen -t ed25519 -C "github-actions" -f ~/.ssh/github_actions -N "" && cat ~/.ssh/github_actions.pub >> ~/.ssh/authorized_keys

cat ~/.ssh/github_actions

-----BEGIN OPENSSH PRIVATE KEY-----
b3BlbnNzaC1rZXktdjEAAAAABG5vbmUAAAAEbm9uZQAAAAAAAAABAAAAMwAAAAtzc2gtZW
QyNTUxOQAAACBaD/zd7W1QwTDcfnD/NRzuhlKUImvB6szRz1FpRG0S7QAAAJiVDKsYlQyr
GAAAAAtzc2gtZWQyNTUxOQAAACBaD/zd7W1QwTDcfnD/NRzuhlKUImvB6szRz1FpRG0S7Q
AAAEDkSSIB0s67zrZ9vjMWvptkJMwBymKaVxNp7RgI67ncC1oP/N3tbVDBMNx+cP81HO6G
UpQia8HqzNHPUWlEbRLtAAAADmdpdGh1Yi1hY3Rpb25zAQIDBAUGBw==
-----END OPENSSH PRIVATE KEY-----

Sur votre navigateur, allez sur votre dépôt GitHub dans Settings > Secrets and variables > Actions et ajoutez ces 3 secrets (New repository secret) :

    VPS_HOST : 51.254.219.63

    VPS_USER : ubuntu

    VPS_SSH_KEY : (Collez le texte de la clé privée copié juste avant)

nano /var/www/QuadGPT/docker-compose.prod.yml et collé ce code : 

```
version: "3.9"

services:
  backend:
    image: ghcr.io/${GITHUB_REPOSITORY}/backend:latest
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/${DB_NAME}
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    depends_on:
      - database
    ports:
      - "8080:8080"
    networks:
      - app-network
    restart: unless-stopped

  frontend:
    image: ghcr.io/${GITHUB_REPOSITORY}/frontend:latest
    ports:
      - "80:80"
    networks:
      - app-network
    restart: unless-stopped

  adminer:
    image: adminer
    ports:
      - "8082:8080"
    networks:
      - app-network
    restart: unless-stopped

  database:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USER}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - db-data:/var/lib/postgresql/data
    networks:
      - app-network
    restart: unless-stopped

networks:
  app-network:

volumes:
  db-data:
```  

À la racine de votre projet Git local, créez la structure de dossiers suivante s'elle n'existe pas :
.github/workflows/

Dans ce dossier, créez le fichier .github/workflows/deploy.yml et collez-y :

```
name: CI/CD Pipeline - OVH VPS

on:
  push:
    branches: [ "main" ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - name: Récupérer le code
        uses: actions/checkout@v4

      - name: Connexion au registre GitHub (GHCR)
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Lowercase Repository Name
        run: echo "REPO_LOWER=${GITHUB_REPOSITORY,,}" >> $GITHUB_ENV

      - name: Build & Push Backend
        uses: docker/build-push-action@v5
        with:
          context: ./backend
          file: ./backend/Dockerfile
          push: true
          tags: ghcr.io/${{ env.REPO_LOWER }}/backend:latest

      - name: Build & Push Frontend
        uses: docker/build-push-action@v5
        with:
          context: ./frontend
          file: ./frontend/Dockerfile
          push: true
          tags: ghcr.io/${{ env.REPO_LOWER }}/frontend:latest

      - name: Déploiement SSH sur le VPS
        uses: appleboy/ssh-action@v1.0.3
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USER }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            cd /var/www/QuadGPT
            export GITHUB_REPOSITORY="${{ env.REPO_LOWER }}"
            echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
            docker compose -f docker-compose.prod.yml pull
            docker compose -f docker-compose.prod.yml up -d --remove-orphans
```            