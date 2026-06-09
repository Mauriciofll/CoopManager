# CoopManager

Protótipo simples em Java para a matéria de extensão Projeto Empreendedor.

## Propósito

O CoopManager foi pensado como um ajudante para quem atua como intermediador dentro de uma cooperativa ou associação. A ideia não é transformar o trabalho em um sistema de vendas completo, nem automatizar decisões, negociações ou relações entre produtores e clientes. O app serve para organizar informações, reduzir esquecimentos e dar mais visibilidade ao que está acontecendo em cada pedido.

Na prática, ele ajuda o intermediador a acompanhar produtos cadastrados pelos produtores, registrar pedidos recebidos como controle operacional, saber quais itens ainda precisam ser enviados, conferir o que chegou na cooperativa e manter um histórico simples do andamento. Assim, a pessoa responsável pela ponte entre produtores, comerciantes e clientes consegue trabalhar com menos anotações soltas e mais clareza sobre pendências.

O foco do projeto é apoio operacional: mostrar o estado dos pedidos, das quantidades enviadas e recebidas, dos prazos e dos responsáveis. O CoopManager não substitui conversa, conferência física dos produtos, controle financeiro, emissão fiscal, pagamento, entrega real ou tomada de decisão humana.

## O que este app não é

- Não é um sistema de vendas completo.
- Não é um caixa, ERP, marketplace ou e-commerce.
- Não automatiza pedidos, pagamentos, entregas ou negociações.
- Não decide sozinho se um produtor cumpriu ou não o combinado.
- Não substitui o intermediador; apenas organiza informações para ajudar essa pessoa a agir melhor.

## Funcionalidades

- Cadastro de produtos dos associados
- CRUD de produtos por produtor logado
- Catálogo digital simples
- Controle de estoque
- Estoque mínimo configurável por produto
- Login de produtores da cooperativa
- CRUD de usuários administradores/produtores e clientes
- Registro operacional de pedidos recebidos com múltiplos produtos
- Data automática do pedido e data prevista opcional para entrega ou retirada
- Indicadores de pedidos previstos para hoje e atrasados
- Painel de alertas do intermediador com atrasos, itens em trânsito, pendências do produtor e divergências de recebimento
- Resumo dos itens pendentes que ainda precisam ser recebidos dos produtores
- Orientações contextuais discretas nas áreas de cadastro, acompanhamento e filtros
- Ações contextuais na aba de pedidos, mostrando apenas operações úteis para o item e etapa selecionados
- Seletores de pedido mais limpos, com produto exibido pelo nome, preço e produtor
- Campos de quantidade exibidos somente em envios ou recebimentos parciais
- Listas operacionais para orientar produtor, conferência na cooperativa e separação por cliente
- Avanço operacional dos pedidos com poucas ações manuais
- Controle por item de quantidade enviada pelo produtor, inclusive envio parcial, e quantidade recebida pela cooperativa
- Filtros operacionais para localizar pedidos com divergência, aguardando produtor, em trânsito, atrasados, previstos para hoje ou prontos para separação
- Histórico operacional do pedido com usuário, data/hora, ação e observação opcional
- Cancelamento e exclusão de pedidos por administradores, com devolução de estoque quando necessário
- Proteção de histórico para evitar excluir produtos, produtores ou clientes que já aparecem em pedidos
- Filtros rápidos por texto, status e baixo estoque
- Apoio à logística de entrega ou retirada
- Interface gráfica com modo claro/noturno salvo por usuário para visualizar resumo, produtos, clientes e pedidos
- Ajustes de layout para notebooks e telas menores, com formulários roláveis e filtros menos espremidos
- Versão mobile/PWA em `mobile/`, adaptada para uso por toque com resumo, pedidos, produtos e listas operacionais

## Como executar

O projeto pode salvar os dados em arquivo local ou em PostgreSQL. Sem configuração extra, ele usa o modo local para continuar abrindo sem dependências externas.

Para uma explicação pronta para entregar ou apresentar ao cliente, use o [Tutorial de uso do CoopManager](docs/TUTORIAL_DE_USO.md).

Entre na pasta do projeto e compile:

```bash
javac -d build/classes src/Main.java src/database/*.java src/security/*.java src/view/*.java src/controller/*.java src/repository/*.java src/model/*.java
java -cp "build/classes;lib/*" Main
```

Ao executar pela primeira vez em uma instalação sem banco local, a aplicação abre a tela `Primeiro acesso` para criar o primeiro administrador. Depois disso, ela passa a abrir a janela de login e as abas `Resumo`, `Produtos`, `Usuários`, `Clientes` e `Pedidos`.

No modo local, o banco fica em `%APPDATA%\CoopManager\coopmanager.dat`. Se existir um arquivo antigo em `data/coopmanager.dat`, ele será copiado automaticamente na primeira execução.

## PostgreSQL

Para usar vários computadores com os mesmos dados, o caminho recomendado é ter um computador principal com PostgreSQL instalado e fazer os outros computadores apontarem para ele.

Se você ainda não tem PostgreSQL instalado ou precisa de uma orientação bem passo a passo, comece pelo guia [Banco compartilhado do CoopManager](docs/BANCO_COMPARTILHADO.md). Ele explica a ideia do computador principal, qual versão instalar, o que marcar no instalador do PostgreSQL no Windows e como testar com dois computadores.

O build do CoopManager baixa o driver JDBC PostgreSQL automaticamente para `lib/` quando ele não existe.

No computador principal, depois de instalar o PostgreSQL, o banco pode ser criado pelo script assistido:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/criar-banco-postgres-compartilhado.ps1 -HostName localhost -Database coopmanager -AppUser coopmanager -ConfigureThisComputer
```

Também é possível configurar manualmente copiando `config/database.example.properties` para `%APPDATA%\CoopManager\database.properties` e ajustando:

```properties
database.mode=postgres
postgres.url=jdbc:postgresql://localhost:5432/coopmanager
postgres.user=coopmanager
postgres.password=sua_senha
```

Também é possível abrir a tela de configuração pelo executável:

```powershell
CoopManager.exe --configurar-banco
```

Ou configurar um computador cliente por PowerShell:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/configurar-banco-compartilhado.ps1 -HostName IP_DO_SERVIDOR -Port 5432 -Database coopmanager -User coopmanager
```

As tabelas são criadas automaticamente na primeira conexão. Senhas novas são gravadas com hash PBKDF2.

## Primeiro acesso

Em uma instalação zerada, o CoopManager não cria usuários automáticos. Na primeira abertura, preencha:

- Nome do primeiro administrador
- Login
- Senha
- Confirmação da senha

Esse usuário será criado como administrador e já poderá cadastrar produtores, clientes, produtos e pedidos. Depois do primeiro acesso, novas aberturas do aplicativo usam a tela normal de login.

## Dados de demonstração opcionais

Os dados de exemplo não são carregados automaticamente na versão zerada. Para abrir uma base de demonstração em um ambiente sem usuários, rode o Java com a propriedade `coopmanager.demo=true` ou defina a variável de ambiente `COOPMANAGER_DEMO=true`.

Quando o modo demonstração é usado em uma base vazia, estes logins são criados:

- Produtor da Agricultura: login `agricultura`, senha `agro123`
- Artesão Associado: login `artesao`, senha `arte123`
- Administrador: login `admin`, senha `admin123`

Cada produtor visualiza e gerencia apenas os próprios produtos. Na aba `Produtos`, use o formulário lateral apenas para cadastrar produtos novos. Para alterar ou excluir um produto já existente, dê duplo clique na linha da tabela. O campo `Estoque mínimo` define, produto por produto, quando ele aparece como baixo em estoque no resumo.

Administradores podem cadastrar usuários e clientes, registrar pedidos recebidos como acompanhamento operacional, acompanhar pedidos de todos os produtores ou filtrar por produtor. Para alterar ou excluir usuários e clientes já existentes, dê duplo clique na linha da tabela. Quando um produto, produtor ou cliente já aparece em pedidos, o app preserva esse cadastro para manter o histórico legível. Produtores informam o envio dos itens dos próprios produtos.

Na aba `Resumo`, o intermediador encontra alertas de ação: pedidos atrasados, itens aguardando produtor, itens em trânsito e divergências entre quantidade enviada e quantidade recebida.

Na aba `Pedidos`, o fluxo de recebimento é separado em duas etapas simples: o produtor informa a quantidade enviada de cada item e o administrador confirma o recebimento físico do que foi enviado. A tela evita campos que não podem ser alterados naquele momento: em vez de uma lista de ações, ela mostra a próxima ação útil para o perfil logado. Quando tudo foi movimentado, basta manter marcado `Todos enviados` ou `Todos recebidos`; quando a movimentação for parcial, desmarque essa opção e informe quantas unidades estão saindo ou chegando agora. A etapa do pedido aparece apenas quando há um avanço válido para registrar.

A aba `Listas`, dentro de `Pedidos`, gera listas de trabalho a partir dos filtros atuais: uma lista do produtor com o que ainda precisa sair, uma lista de conferência para o que chegou ou está em trânsito, e uma lista de separação por cliente. Essas listas podem ser copiadas para impressão, mensagem ou conferência manual, mantendo o CoopManager como apoio operacional do intermediador.

Se quiser usar a versão antiga por console, troque temporariamente o `Main` para iniciar `MainConsoleView`.

## Versão mobile/PWA

A pasta `mobile/` contém uma versão mobile adaptada do CoopManager. Ela não é a tela desktop apenas encolhida: a navegação fica na parte inferior, os pedidos aparecem em cards, as ações de envio/recebimento são botões grandes e as listas operacionais foram pensadas para uso rápido no celular.

Essa versão funciona como PWA offline no navegador e usa armazenamento local do próprio aparelho. No primeiro uso, ela também pede a criação de um administrador local e começa sem dados de demonstração. Ela serve como aplicativo mobile de apoio operacional; ainda não sincroniza automaticamente com o banco local/PostgreSQL do desktop.

Para testar no computador, rode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-mobile.ps1
```

Depois abra `http://localhost:8088/mobile/`. Em um celular na mesma rede, use o IP do computador no lugar de `localhost`.

Para liberar acesso por outro aparelho na rede, rode o script informando o IP do computador ou `*` no parâmetro `-Bind`, por exemplo:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/run-mobile.ps1 -Bind *
```

## Gerar APK Android

A versão mobile também pode ser empacotada como APK Android. O projeto Android fica em `mobile-apk/` e abre a versão `mobile/` dentro de um WebView local, mantendo a proposta de ajudante operacional offline. O APK gerado é uma versão debug, boa para teste e instalação manual no celular; para publicar em loja ou distribuir oficialmente, ainda será necessário configurar assinatura de release.

Para gerar o APK, rode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-mobile-apk.ps1
```

O script baixa Gradle e Android SDK para `build/`, cria uma truststore local quando o Java não reconhece os certificados da rede, instala os pacotes Android necessários e copia o resultado para:

```text
dist/mobile/CoopManager-Mobile-debug.apk
```

No Android, instale esse arquivo permitindo instalação de apps desconhecidos para o gerenciador de arquivos usado. Como o app mobile ainda usa armazenamento local do próprio aparelho, os dados do APK não sincronizam automaticamente com o desktop. Em uma instalação nova, o APK abre a configuração inicial antes de mostrar as telas operacionais.

## Visual Studio Code

Use a configuração `Run CoopManager` na aba Run and Debug.

Também é possível usar `Terminal > Run Task > Run CoopManager`.

Se ele mostrar que `Main` já está em execução, clique em `Cancel` e pare a execução anterior antes de iniciar de novo.

## Gerar executável Windows

Use a task `Build CoopManager EXE` no VS Code ou rode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-exe.ps1
```

O executável fica em `dist/CoopManager/CoopManager.exe`.

## Gerar instalador Windows

Para entregar ao cliente, prefira gerar o instalador MSI. Ele usa Windows Installer via WiX/jpackage e evita o empacotamento legado com IExpress, que pode parecer suspeito para alguns antivírus por executar scripts de instalação.

Rode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-msi-installer.ps1
```

Os arquivos principais ficam em:

```text
dist/installer-msi/CoopManager-Setup.msi
dist/portable/CoopManager-Portable.zip
```

O MSI instala o aplicativo, cria atalhos e mantém a instalação zerada. Em um computador novo, o primeiro uso abre a tela de criação do administrador.

O instalador legado em `.exe` ainda pode ser gerado, mas não é recomendado para envio ao cliente:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-installer.ps1
```

O instalador fica em `dist/installer/CoopManager-Setup.exe`. Ele inclui o runtime necessário, permite escolher a pasta de instalação e cria atalhos no menu iniciar e, opcionalmente, na área de trabalho.

Por padrão, o instalador é gerado zerado e não leva o banco local de quem está empacotando. Assim, em um computador novo, o primeiro uso abre a tela de criação do administrador.

### Aviso de antivírus ou SmartScreen

O pacote MSI e o ZIP portable reduzem a chance de falso positivo porque não usam o instalador autoextraível legado com PowerShell/VBS escondido. Mesmo assim, como os arquivos ainda não estão assinados digitalmente com certificado de publicação confiável, o Windows pode mostrar aviso de editor desconhecido ou reputação baixa.

Para eliminar esse problema de forma profissional, é necessário assinar o `CoopManager.exe`, o MSI e o APK de release com certificados reais e distribuir sempre pelo mesmo canal. No Windows, isso significa usar assinatura de código confiável e deixar a reputação do arquivo/publicador crescer com o tempo.

Se for necessário gerar o instalador legado `.exe` com o banco local atual para demonstração ou reprodução de estado, rode:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-installer.ps1 -IncludeLocalData
```

Nesse modo, quando há um banco local em `%APPDATA%\CoopManager\coopmanager.dat`, o script de build inclui esse arquivo no instalador. Durante a instalação, se já existir um banco no destino, ele é copiado para um backup antes de ser substituído.

## Status do pedido

- AGUARDANDO PRODUTORES
- AGUARDANDO RECEBIMENTO
- PARCIALMENTE RECEBIDO
- PENDÊNCIA
- EM SEPARAÇÃO
- PRONTO
- ENTREGUE
- CANCELADO

## Status do item do pedido

- PENDENTE
- ENVIADO À COOPERATIVA
- ENTREGUE À COOPERATIVA
- INDISPONÍVEL
- CANCELADO
