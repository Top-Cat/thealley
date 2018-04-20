FROM node:carbon-alpine

WORKDIR /app

COPY package.json .
RUN npm install
COPY . .
RUN patch -p0 < subdevice.patch
RUN patch -p0 < developer-api.patch

EXPOSE 8080
CMD [ "npm", "start" ]
