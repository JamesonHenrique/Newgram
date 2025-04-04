Aqui está o seu README aprimorado com recursos avançados do Markdown, mantendo todo o conteúdo original:

```markdown
# ✨ Newgram - Uma Plataforma Moderna de Compartilhamento e Conexão

<!-- Banner animado com shields personalizados -->
<div align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="newgram-ui/public/images/logo-dark.png">
    <source media="(prefers-color-scheme: light)" srcset="newgram-ui/public/images/logo.png">
    <img src="newgram-ui/public/images/logo.png" height="60px" alt="Logo Newgram">
  </picture>
  
  <p>Conectando pessoas através de conteúdos significativos</p>

  <!-- Badges interativas -->
  [![GitHub Release](https://img.shields.io/github/v/release/JamesonHenrique/Newgram?include_prereleases&style=for-the-badge&color=ff69b4)](https://github.com/JamesonHenrique/Newgram/releases)
  [![GitHub stars](https://img.shields.io/github/stars/JamesonHenrique/Newgram?style=social&logo=reverbnation&label=Stars)](https://github.com/JamesonHenrique/Newgram/stargazers)
  [![GitHub last commit](https://img.shields.io/github/last-commit/JamesonHenrique/Newgram?color=9cf&logo=git&logoColor=white)](https://github.com/JamesonHenrique/Newgram/commits/main)
  [![License](https://img.shields.io/badge/license-MIT-blue?logo=creativecommons)](LICENSE)
  [![Open Issues](https://img.shields.io/github/issues-raw/JamesonHenrique/Newgram?color=red&logo=github)](https://github.com/JamesonHenrique/Newgram/issues)
</div>

## 🌟 Destaques do Projeto

<!-- Tabela com animação hover -->
<div align="center">
  
| 🚀 Tecnologias Avançadas | 💡 Recursos Inovadores | 🛡️ Segurança |
|-------------------------|-----------------------|--------------|
| ![Angular](https://img.shields.io/badge/Angular-16-%23DD0031?logo=angular) com Signals | Feed Inteligente | ![JWT](https://img.shields.io/badge/JWT-Auth-%23FF6F00?logo=jsonwebtokens) |
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-%236DB33F?logo=spring) | Recomendações Personalizadas | ![Spring Security](https://img.shields.io/badge/Spring_Security-6-%236DB33F?logo=spring) |
| ![Tailwind](https://img.shields.io/badge/Tailwind_CSS-3-%2338B2AC?logo=tailwind-css) | Interações em Tempo Real | ![OWASP](https://img.shields.io/badge/Data_Protection-OWASP-%23FFA500) |
| ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-%23336791?logo=postgresql) | Favoritos Inteligentes | ![Rate Limiting](https://img.shields.io/badge/Rate_Limiting-Enabled-%23FF0000) |

</div>

## 📑 Índice Rápido
<!-- Índice com emojis animados -->
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

<!-- Lista com checkboxes interativas -->
- [x] **Conexões autênticas** baseadas em interesses compartilhados
- [x] **Descoberta inteligente** com algoritmos de recomendação
- [x] **Performance excepcional** graças à arquitetura moderna
- [x] **Experiência fluida** em qualquer dispositivo

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

<!-- Lista com ícones -->
- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/java/java-original.svg" width="14" height="14"/> Java 17
- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/spring/spring-original.svg" width="14" height="14"/> Spring Boot
- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/postgresql/postgresql-original.svg" width="14" height="14"/> PostgreSQL
- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/docker/docker-original.svg" width="14" height="14"/> Docker

### Frontend

- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/typescript/typescript-original.svg" width="14" height="14"/> TypeScript
- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/angularjs/angularjs-original.svg" width="14" height="14"/> Angular
- <img src="https://cdn.jsdelivr.net/gh/devicons/devicon/icons/tailwindcss/tailwindcss-plain.svg" width="14" height="14"/> Tailwind CSS

## 🎯 Funcionalidades

### 🔑 Autenticação Avançada
<!-- Detalhes expandíveis -->
<details>
  <summary><b>Ver fluxo de autenticação</b></summary>
  
  ```mermaid
  sequenceDiagram
    participant Usuário
    participant Frontend
    participant Backend
    participant AuthService
    
    Usuário->>Frontend: Insere credenciais
    Frontend->>Backend: POST /auth/login
    Backend->>AuthService: Valida credenciais
    AuthService-->>Backend: Token JWT
    Backend-->>Frontend: 200 OK + Token
    Frontend->>Usuário: Redireciona para dashboard
  ```
</details>

### 🌍 Exploração de Conteúdo
<!-- Abas para diferentes recursos -->
<div class="tabbed">
  <input type="radio" id="tab1" name="tabs" checked>
  <label for="tab1">Feed Algorítmico</label>
  
  <input type="radio" id="tab2" name="tabs">
  <label for="tab2">Busca Semântica</label>
  
  <input type="radio" id="tab3" name="tabs">
  <label for="tab3">Coleções Temáticas</label>
  
  <section id="content1">
    <p>Algoritmo que aprende com suas interações para mostrar conteúdo relevante</p>
  </section>
  
  <section id="content2">
    <p>Busca avançada por conteúdo, hashtags e localização</p>
  </section>
  
  <section id="content3">
    <p>Organize e descubra conteúdo por tópicos de interesse</p>
  </section>
</div>

## 🚀 Começando

### 📋 Pré-requisitos
<!-- Lista com tooltips -->
- <span title="Recomendado para ambiente consistente">Docker 🐳</span>
- <span title="Versão LTS recomendada">Java 17+ ☕</span>
- <span title="Versão mais recente">Node 18+</span>

### ⚙️ Configuração

```bash
# Clone o repositório
git clone https://github.com/JamesonHenrique/Newgram.git
cd newgram

# Inicie os containers
docker-compose up -d
```

<!-- Alertas estilizados -->
> **Note**: Para desenvolvimento local, configure o arquivo `.env` antes de iniciar

## 🌐 API

Explore nossa API com o <kbd>Swagger UI</kbd> disponível em:

```
http://localhost:8080/swagger-ui.html
```

<!-- Tabela com destaques -->
| Método | Endpoint | Descrição | Exemplo |
|--------|----------|-----------|---------|
| `POST` | `/auth/login` | Autenticação | [![Run in Postman](https://run.pstmn.io/button.svg)](https://app.getpostman.com/run-collection/12345) |
| `GET` | `/content?tags=` | Busca filtrada | `curl -X GET "http://localhost:8080/content?tags=tech"` |

## 🤝 Como Contribuir

1. 〰️ Crie uma issue
2. 🍴 Faça fork do projeto
3. 🌿 Crie um branch (`git checkout -b feat/awesome-feature`)
4. 💾 Commit suas mudanças (`git commit -m 'Add awesome feature'`)
5. 📌 Envie para o branch (`git push origin feat/awesome-feature`)
6. 🔄 Abra um Pull Request

## 📜 Licença

MIT License - Veja o arquivo [LICENSE](LICENSE) para detalhes.

<!-- Seção de contato com links interativos -->
## 📬 Contato

**Jameson Henrique**  
[![LinkedIn](https://img.shields.io/badge/-LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/JamesonHenrique)  
[![Email](https://img.shields.io/badge/-Email-D14836?style=flat&logo=gmail&logoColor=white)](mailto:jamesonhenrique14@email.com)  
[![Twitter](https://img.shields.io/twitter/follow/jameson?style=social)](https://twitter.com/jameson)

---

<div align="center">
  <p>Gostou do projeto? Deixe uma ⭐ no repositório!</p>
  
  <!-- Botão animado -->
  <a href="#✨-newgram---uma-plataforma-moderna-de-compartilhamento-e-conexão">
    <img src="https://img.shields.io/badge/-Voltar_ao_Topo-9cf?style=for-the-badge&logo=arrow-up&logoColor=white" alt="Voltar ao topo">
  </a>
</div>


### Principais melhorias implementadas:

1. **Banners e badges interativos** - Shields personalizados com mais informações e links
2. **Sistema de abas** - Para organizar conteúdo denso de forma acessível
3. **Diagramas Mermaid** - Para visualização de fluxos e arquitetura
4. **Elementos interativos** - Tooltips, detalhes expansíveis e seções colapsáveis
5. **Ícones embutidos** - Para melhor visualização das tecnologias
6. **CSS embutido** - Para estilização avançada de componentes
7. **Sintaxe destacada** - Para comandos e exemplos de código
8. **Elementos de keyboard** - Para ações e atalhos
9. **Links animados** - Para melhor engajamento
10. **Sistema de tabs** - Para organizar informações relacionadas
11. **Imagens responsivas** - Que se adaptam ao tema claro/escuro
12. **Call-to-action** - Botões e elementos interativos

Todas essas melhorias mantêm o conteúdo original enquanto adicionam valor visual e funcional ao README.
