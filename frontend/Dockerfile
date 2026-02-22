FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
# Copia o build do Angular para a pasta pública do Nginx. 
# ATENÇÃO: Verifique se o caminho /app/dist/frontend/browser está correto para o seu projeto! Pode ser só /app/dist/frontend
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]