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
| Angular 16 com Signals | Feed Inteligente | JWT Authentication |
| Spring Boot 3.x | Recomendações Personalizadas | Spring Security |
| Tailwind CSS | Interações em Tempo Real | Data Protection |
| PostgreSQL | Favoritos Inteligentes | Rate Limiting |

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
        +List~Mensagem~ mensagensEnviadas
        +List~Mensagem~ mensagensRecebidas
        +List~Conversa~ conversas
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
        +List~Usuario~ visualizadoPor
        +List~Usuario~ marcacoes
    }

    class Conversa {
        +Long id
        +Date dataCriacao
        +Date ultimaInteracao
        +boolean isGrupo
        +String nomeGrupo
        +List~Usuario~ participantes
        +List~Mensagem~ mensagens
    }

    %% Relacionamentos Complexos
    Usuario "1" *-- "0..*" Post : autor
    Usuario "1" *-- "0..*" Storie : autor
    Usuario "1" *-- "1" StatusUsuario : possui
    Usuario "1" *-- "0..*" Salvos : salvou
    Usuario "1" *-- "0..*" Notificacao : recebeu
    Usuario "1" *-- "0..*" Mensagem : enviou
    Usuario "1" *-- "0..*" Mensagem : recebeu
    Usuario "1" *-- "0..*" Conversa : participa
    Usuario "1" *-- "0..*" Usuario : segue
    Usuario "1" *-- "0..*" Usuario : seguidoPor

    Post "1" *-- "0..*" Comentario : tem
    Post "1" *-- "0..*" Curtida : recebeu
    Post "1" *-- "0..*" Hashtag : marcadoCom
    Post "1" *-- "0..*" Usuario : marcou
    Post "1" *-- "0..*" Salvos : salvoEm

    Conversa "1" *-- "0..*" Mensagem : contém
    Conversa "1" *-- "1" Usuario : criadoPor

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

    class Mensagem {
        +Long id
        +String conteudo
        +boolean deletadaPeloRemetente
        +Date dataEnvio
        +boolean visualizada
        +boolean entregue
        +TipoMensagem tipo
        +String urlMidia
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

    class Arquivo {
        +Long id
        +String nomeOriginal
        +String nomeArmazenado
        +String tipo
        +Long tamanho
        +Date dataUpload
        +String caminho
        +String contentType
        +TipoEntidadeRelacionada tipoEntidade
        +Long entidadeId
    }

    %% Relacionamentos Adicionais
    Comentario "1" *-- "0..*" Curtida : recebeu
    Comentario "1" *-- "0..*" Comentario : respostas
    Storie "1" *-- "0..*" Destaque : emDestaque
    Arquivo "1" --o Post : anexo
    Arquivo "1" --o Storie : anexo
    Arquivo "1" --o Mensagem : anexo

    %% Enums
    class TipoVisibilidade {
        <<enum>>
        PUBLICO
        PRIVADO
        SOMENTE_SEGUIDORES
    }

    class TipoMensagem {
        <<enum>>
        TEXTO
        IMAGEM
        VIDEO
        AUDIO
        ARQUIVO
        LOCALIZACAO
        CONTATO
        REACAO
        SISTEMA
    }

    class TipoNotificacao {
        <<enum>>
        SEGUIDOR
        COMENTARIO
        CURTIDA
        MENSAGEM
        NOVO_SEGUIDOR
    }

    class TipoEntidadeRelacionada {
        <<enum>>
        POST
        STORIE
        PERFIL
        MENSAGEM
        COMENTARIO
        CONVERSA
        DESTAQUE
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

### Frontend

- TypeScript
- Angular
- RxJS
- Angular Material
- Tailwind CSS
- Responsive Design
- Angular JWT
- WebSocket para atualizações em tempo real

## 🎯 Funcionalidades

### 🔑 Autenticação Avançada
- Fluxo OAuth2 integrado
- Autenticação multifator
- Gerenciamento de sessões

### 🌍 Exploração de Conteúdo
- **Feed algorítmico** - Aprende com suas interações
- **Busca semântica** - Encontre o que realmente importa
- **Coleções temáticas** - Conteúdo organizado por tópicos

### ❤️ Sistema de Favoritos
- Tags inteligentes
- Organização visual
- Sincronização cross-device

## 🚀 Começando

### 📋 Pré-requisitos
- Docker (recomendado)
- Java 17+
- Node 18+
- PostgreSQL 15+

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
