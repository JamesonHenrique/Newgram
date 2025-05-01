# ✨ Newgram - Uma Plataforma Moderna de Compartilhamento e Conexão

<div align="center">
  <picture>
    <source  srcset="newgram-ui/public/images/logo.png">
    <img src="newgram-ui/public/images/logo.png" height="60px" alt="Logo Newgram">
  </picture>

  <p>Conectando pessoas através de conteúdos significativos</p>

  [![GitHub Release](https://img.shields.io/github/v/release/JamesonHenrique/Newgram?include_prereleases&style=for-the-badge&color=ff69b4)](https://github.com/JamesonHenrique/Newgram/releases)
  [![GitHub stars](https://img.shields.io/github/stars/JamesonHenrique/Newgram?style=social&logo=reverbnation&label=Stars)](https://github.com/JamesonHenrique/Newgram/stargazers)
  [![GitHub last commit](https://img.shields.io/github/last-commit/JamesonHenrique/Newgram?color=9cf&logo=git&logoColor=white)](https://github.com/JamesonHenrique/Newgram/commits/main)
  [![License](https://img.shields.io/badge/license-MIT-blue?logo=creativecommons)](LICENSE)
  [![Open Issues](https://img.shields.io/github/issues-raw/JamesonHenrique/Newgram?color=red&logo=github)](https://github.com/JamesonHenrique/Newgram/issues)
</div>

## 🌟 Destaques do Projeto

<div align="center">

| 🚀 Tecnologias Avançadas | 💡 Recursos Inovadores | 🛡️ Segurança |
|-------------------------|-----------------------|--------------|
| Angular | Feed Inteligente | JWT Authentication |
| Spring Boot | Recomendações Personalizadas | Spring Security |
| Tailwind CSS | Design Incrivel | Dados Seguros |
| PostgreSQL | Favoritos Inteligentes | Protegido de Ataques |

</div>

## 📑 Índice Rápido
- [✨ Visão Geral](#-visão-geral)
- [🛠️ Tecnologias](#️-tecnologias)
- [🎯 Funcionalidades](#-funcionalidades)
- [🚀 Começando](#-começando)
  - [📋 Pré-requisitos](#-pré-requisitos)
  - [⚙️ Configuração](#️-configuração)
- [🌐 API](#-api)
- [🤝 Como Contribuir](#-como-contribuir)
- [📜 Licença](#-licença)
- [📬 Contato](#-contato)

## ✨ Visão Geral

O Newgram redefine a experiência de compartilhamento de conteúdo, oferecendo:

- **Conexões autênticas** baseadas em interesses compartilhados
- **Descoberta inteligente** com algoritmos de recomendação
- **Performance excepcional** graças à arquitetura moderna
- **Experiência fluida** em qualquer dispositivo

### 🎯 Diagrama de Classes

```mermaid
classDiagram
    %% Entidades Principais
    class Usuario {
        +Long id
        +String nome
        +String username
        +String email
        +String senha
        +String bio
        +Date dataCriacao
        +List~Post~ posts
        +List~Usuario~ seguidores
        +List~Usuario~ seguindo
        +List~Salvos~ postsSalvos
        +List~Notificacao~ notificacoes
        +StatusUsuario statusUsuario
    }

    class Post {
        +Long id
        +String legenda
        +Date dataCriacao
        +String localizacao
        +boolean arquivado
        +TipoVisibilidade visibilidade
        +List~Comentario~ comentarios
        +List~Curtida~ curtidas
        +List~Hashtag~ hashtags
        +List~Usuario~ marcacoes
        +List~Salvos~ salvosPor
    }

    class Storie {
        +Long id
        +Date dataCriacao
        +Date dataExpiracao
        +boolean destacado
        +String storieImagemUrl
        +List~Usuario~ visualizadoPor
        +List~Usuario~ marcacoes
    }



    %% Relacionamentos Complexos
    Usuario "1" *-- "0..*" Post : autor
    Usuario "1" *-- "0..*" Storie : autor
    Usuario "1" *-- "1" StatusUsuario : possui
    Usuario "1" *-- "0..*" Salvos : salvou
    Usuario "1" *-- "0..*" Notificacao : recebeu

    Usuario "1" *-- "0..*" Usuario : segue
    Usuario "1" *-- "0..*" Usuario : seguidoPor

    Post "1" *-- "0..*" Comentario : tem
    Post "1" *-- "0..*" Curtida : recebeu
    Post "1" *-- "0..*" Hashtag : marcadoCom
    Post "1" *-- "0..*" Usuario : marcou
    Post "1" *-- "0..*" Salvos : salvoEm


    %% Classes de Suporte
    class Comentario {
        +Long id
        +String texto
        +Date dataCriacao
        +List~Curtida~ curtidas
    }

    class Curtida {
        +Long id
        +Date dataCriacao
    }

    class Hashtag {
        +Long id
        +String nome
    }

    class Salvos {
        +Long id
        +Date dataSalvo
        +String colecao
    }

    class Notificacao {
        +Long id
        +TipoNotificacao tipo
        +String conteudo
        +Date dataCriacao
        +boolean lida
    }

    class StatusUsuario {
        +Long id
        +boolean online
        +Date ultimoAcesso
        +String statusPersonalizado
    }

    class Destaque {
        +Long id
        +String nome
        +Date dataCriacao
        +List~Storie~ stories
    }


    %% Relacionamentos Adicionais
    Comentario "1" *-- "0..*" Curtida : recebeu
    Comentario "1" *-- "0..*" Comentario : respostas
    Storie "1" *-- "0..*" Destaque : emDestaque

    %% Enums
    class TipoVisibilidade {
        <<enum>>
        PUBLICO
        PRIVADO
        SOMENTE_SEGUIDORES
    }


    class TipoNotificacao {
        <<enum>>
        SEGUIDOR
        COMENTARIO
        CURTIDA
        MENSAGEM
        NOVO_SEGUIDOR
    }

    class TipoArquivo {
        <<enum>>
        POST
        FOTO_PERFIL
        STORIE
        FOTO_DESTAQUE

    }
```

## 🛠️ Tecnologias


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
- AWS
- S3

### Frontend

- TypeScript
- Angular
- RxJS
- Animate CSS
- Tailwind CSS
- Angular JWT
- NG-Openapi-Gen
  
## 🎯 Funcionalidades

### 🔑 Autenticação Avançada
- Token e Refreshs token seguros
- Gerenciamento de erros

### 🌍 Exploração de Conteúdo
- **Feed infinito** - Aparece mais posts, até acabar todos
- **Busca semântica** - Encontre o que realmente importa
- **Criar Posts, stories e destaques** - Apresente ao mundo o que deseja

### ❤️ Sistema de Favoritos
- Tags inteligentes
- Organização visual

## 🚀 Começando

### 📋 Pré-requisitos

- Java
- Angular
- PostgreSQL 

## Instalação

### Backend

1. Clone o repositório:

```bash
git clone https://github.com/JamesonHenrique/Newgram.git
cd newgram
```

2. Configure o banco de dados PostgreSQL no arquivo `src/main/resources/application.properties`

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

## 🌐 API

A documentação da API está disponível através do Swagger UI:

```
http://localhost:8080/swagger-ui.html
```

## 🌍 Acesse o Projeto

<div align="center">

[![Demo Newgram](https://img.shields.io/badge/🚀_Acesse_a_Demo_Newgram-FF6B6B?style=for-the-badge&logo=vercel&logoColor=white)](https://newgram-nine.vercel.app/)

<p>Experimente agora mesmo a nova geração de redes sociais!</p>

[![Web Preview](https://img.shields.io/badge/📱_Mobile_Ready-9cf?style=flat-square)]() 
[![PWA](https://img.shields.io/badge/📲_Instalável_PWA-4285F4?style=flat-square&logo=progressive-web-apps)]() 
[![Performance](https://img.shields.io/badge/⚡_High_Performance-00C58E?style=flat-square&logo=pagespeed-insights)]()

</div>

Principais endpoints:

| Método | Endpoint | Descrição | Exemplo |
|--------|----------|-----------|---------|
| `POST` | `/auth/login` | Autenticação | [![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/12345) |
| `GET` | `/content?tags=` | Busca filtrada | `curl -X GET "http://localhost:8080/content?tags=tech"` |


## 🤝 Como Contribuir

Siga nosso fluxo de colaboração:

1. Crie uma issue descrevendo sua proposta
2. Faça fork do projeto
3. Crie um branch descritivo (`feat/new-auth-flow`)
4. Envie seu PR com:
   - Descrição clara
   - Screenshots (se aplicável)
   - Testes atualizados

## 📜 Licença

MIT License - Veja o arquivo [LICENSE](LICENSE) para detalhes.

## 📬 Contato

**Jameson Henrique**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin)](https://linkedin.com/in/JamesonHenrique)  
[![Email](https://img.shields.io/badge/Email-D14836?style=flat&logo=gmail)](mailto:jamesonhenrique14@email.com)

---


<div align="center">
  <p>Gostou do projeto? Deixe uma ⭐ no repositório!</p>

  <a href="#-newgram---uma-plataforma-moderna-de-compartilhamento-e-conexão">
    <img src="https://img.shields.io/badge/-Voltar_ao_Topo-9cf?style=for-the-badge&logo=arrow-up&logoColor=white" alt="Voltar ao topo">
  </a>
</div>
