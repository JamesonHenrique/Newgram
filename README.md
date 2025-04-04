<div align="center">
 <img src="newgram-ui/public/images/logo.png" height=60px alt="Logo"> 
</div>

## Índice
<div align="center">
| Essenciais                          | Desenvolvimento             | Documentação                  |
| -------------------------------------- | ------------------------------ | -------------------------------- |
| [Sobre](#sobre)                     | [Tecnologias](#tecnologias) | [API](#api)                   |
| [Funcionalidades](#funcionalidades) | [Requisitos](#requisitos)   | [Contribuição](#contribuindo) |
| [Objetivos](#objetivos)             | [Instalação](#instalação)   | [Licença](#licença)           |
</div>


## Sobre

Newgram é uma aplicação web de compartilhamento e descoberta de conteúdo, focada em conectar pessoas através de interesses comuns e experiências compartilhadas. A plataforma permite aos usuários explorar, favoritar e interagir com diversos tipos de conteúdo de forma intuitiva e envolvente.

### Objetivos

- Criar uma plataforma de descoberta de conteúdo personalizada
- Facilitar a conexão entre usuários com interesses semelhantes
- Oferecer uma experiência de navegação intuitiva e agradável
- Permitir interações significativas através de favoritos e exploração
- Promover a diversidade de conteúdo


### Diferenciais

- Interface moderna e responsiva
- Sistema de recomendação personalizado
- Exploração de conteúdo intuitiva
- Funcionalidade de favoritos
- Design minimalista e elegante
- Carregamento rápido e eficiente

## Tecnologias

### Backend

- Java 17
- Spring Boot
- Spring Security
- Spring Data JPA
- PostgreSQL
- Flyway
- Maven
- Swagger
- JWT Authentication

### Frontend

- TypeScript
- Angular
- RxJS
- Angular Material
- Tailwind CSS
- Responsive Design
- Angular JWT
- WebSocket para atualizações em tempo real

## Funcionalidades

### Autenticação

- Registro de usuários
- Login seguro
- Recuperação de senha
- Autenticação JWT

### Exploração de Conteúdo

- Feed personalizado
- Filtros de busca avançados
- Recomendações baseadas em interesses
- Visualização detalhada de conteúdo

### Favoritos

- Adicionar e remover favoritos
- Categorização de favoritos
- Sincronização entre dispositivos
- Compartilhamento de favoritos

### Perfil de Usuário

- Personalização de perfil
- Histórico de interações
- Configurações de privacidade
- Estatísticas de uso

### Notificações

- Notificações em tempo real
- Alertas personalizados
- Configurações de notificação

## Requisitos

- Java 17+
- Node.js 18+
- Angular CLI
- PostgreSQL 12+
- Maven 3.6+

## Instalação

### Backend

1. Clone o repositório:

```bash
git clone https://github.com/JamesonHenrique/Newgram.git
cd newgram
```

2. Configure o banco de dados PostgreSQL no arquivo `src/main/resources/application.yml`

3. Execute o backend:

```bash
mvn spring-boot:run
```

O servidor estará disponível em `http://localhost:8080`

### Frontend

1. Navegue até a pasta do frontend:

```bash
cd newgram-ui
```

2. Instale as dependências:

```bash
npm install
```

3. Execute o frontend:

```bash
ng serve
```

A aplicação estará disponível em `http://localhost:4200`

## API

A documentação da API está disponível através do Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

### Principais Endpoints
- `/auth` - Autenticação
- `/explorar` - Exploração de conteúdo
- `/favoritos` - Gerenciamento de favoritos
- `/perfis` - Perfil de usuário

## Contribuindo

1. Faça o fork do projeto
2. Crie sua feature branch (`git checkout -b feature/NovaFeature`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/NovaFeature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

<div align="center">

Se este projeto te ajudou, considere dar uma estrela!

[ Voltar ao topo](#sobre)

</div>
