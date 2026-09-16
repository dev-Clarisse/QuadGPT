# Lancement du projet

Ouvrir Docker desktop
cd C:\Users\clari\Documents\EPF\5A\intro_IA_géné_chatbot\QuadGPT
docker compose up OU docker compose up --build (Si le code a été modifié)
docker compose ps (Pour vérifier que le front, le back et la BD tournent bien)

# Arrêt du projet 

docker compose down

# Pour se connecter à Adminer, page web pour voir la base de donnée

http://localhost:8082/

système = PostgreSQL
Serveur = database
utilisateur=quadgpt_user
PASSWORD=quadgpt
base de données = quadgpt

# Les ports à ouvrir

- frontend : http://localhost:3000/
- backend : http://localhost:8080/
- PostgreSQL : http://localhost:8082/



