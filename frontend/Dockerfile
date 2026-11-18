# Étape 1 : build Vite
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
# URL d'API vide : le front appelle /api/... sur la même origine, Nginx relaie
ENV VITE_API_URL=""
RUN npm run build

# Étape 2 : Nginx sert le build et relaie /api vers le backend
FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
