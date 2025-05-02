# ✨ Newgram - Uma Plataforma Moderna de Compartilhamento e Conexão

<div align="center">
  <picture>
    <source  srcset="newgram-ui/public/images/logo.png">
    <img src="newgram-ui/public/images/logo.png" height="60px" alt="Logo Newgram">
  </picture>

  <p>Conectando pessoas através de conteúdos significativos</p>

[![Demo Newgram](https://img.shields.io/badge/🚀_Acesse_o_Newgram-392E9F?style=for-the-badge&logo=vercel&logoColor=white)](https://newgram-nine.vercel.app/)

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
    %% ================ MAIN ENTITIES ================
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

    %% ================ SUPPORT CLASSES ================
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

    %% ================ ENUMS ================
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

    %% ================ RELATIONSHIPS ================
    %% User relationships
    Usuario "1" *-- "0..*" Post : cria
    Usuario "1" *-- "0..*" Storie : publica
    Usuario "1" *-- "1" StatusUsuario : possui
    Usuario "1" *-- "0..*" Salvos : salva
    Usuario "1" *-- "0..*" Notificacao : recebe
    
    %% User following relationships
    Usuario "0..*" -- "0..*" Usuario : segue
    
    %% Post relationships
    Post "1" *-- "0..*" Comentario : contém
    Post "1" *-- "0..*" Curtida : recebe
    Post "1" *-- "0..*" Hashtag : marcadoCom
    Post "0..*" -- "0..*" Usuario : marcadoPor
    Post "1" *-- "0..*" Salvos : salvoEm
    
    %% Comment relationships
    Comentario "1" *-- "0..*" Comentario : respondePara
    
    %% Storie relationships
    Storie "0..*" -- "0..*" Usuario : visualizadoPor
    Storie "0..*" -- "0..*" Usuario : marcadoPor
    Storie "0..*" -- "1" Destaque : destacadoEm
    
    %% Like relationships
    Curtida -- Usuario : feitaPor
    Curtida --|> Comentario : opcional
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
