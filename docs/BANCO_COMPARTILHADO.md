# Tutorial passo a passo: banco compartilhado do CoopManager

Este tutorial ensina, passo a passo, como deixar o CoopManager funcionando em mais de um computador com os mesmos dados.

Não precisa saber programar. A ideia é seguir como uma receita.

## A ideia em poucas palavras

Normalmente, cada computador guarda seus próprios dados. Para a cooperativa usar o CoopManager em vários computadores, é melhor ter um computador principal guardando os dados de todos.

Esse computador principal vai ter um programa chamado PostgreSQL. O PostgreSQL é o banco de dados. Ele fica nos bastidores, guardando usuários, produtos, pedidos, quantidades enviadas e quantidades recebidas.

Depois disso:

- O computador principal guarda o banco.
- Os outros computadores acessam esse banco.
- Todo mundo vê os mesmos pedidos.
- Se um produtor registra algo em um computador, o intermediador consegue ver em outro.

O computador principal precisa ficar ligado enquanto a equipe estiver usando o CoopManager.

## O que você vai precisar

- Um computador principal com Windows.
- Internet para baixar o PostgreSQL.
- O instalador do CoopManager.
- A pasta do CoopManager com os scripts de configuração.
- Uma senha anotada para o banco.
- Um pouco de paciência na primeira configuração.

Depois que estiver configurado, o uso do dia a dia é simples: abrir o CoopManager e fazer login.

## Qual versão do PostgreSQL instalar?

Em 09/06/2026, a recomendação para o CoopManager é instalar:

```text
PostgreSQL 18.4 para Windows x86-64
```

Se o site mostrar outra versão `18.x` mais nova, como `18.5`, instale a mais nova da linha 18.

Não instale versão Beta. Por exemplo, se aparecer `PostgreSQL 19 Beta`, não escolha essa opção para o cliente. Beta é versão de teste.

Também não escolha versões antigas como 14, 15 ou 16, a menos que alguém técnico tenha um motivo específico. Para uma instalação nova, use a versão estável mais recente.

## Parte 1: escolher o computador principal

Escolha um computador que:

- Fique na cooperativa ou em um local confiável.
- Fique ligado no horário de uso.
- Tenha rede ou Wi-Fi estável.
- Não seja formatado ou trocado com frequência.

Esse computador será chamado de computador principal neste tutorial.

Ele pode ser um computador comum. Não precisa ser um servidor caro.

## Parte 2: baixar o PostgreSQL

Faça isso no computador principal.

1. Abra o navegador.
2. Entre neste site:

```text
https://www.postgresql.org/download/windows/
```

3. Clique em `Download the installer`.
4. O site vai abrir a página da EDB, que fornece o instalador do PostgreSQL para Windows.
5. Procure a versão `PostgreSQL 18.x`.
6. Na linha do Windows, baixe a opção `Windows x86-64`.

Se aparecerem várias versões, escolha assim:

- Correto: `PostgreSQL 18.x`, Windows `x86-64`.
- Evite: `PostgreSQL 19 Beta`.
- Evite: arquivos `.zip`, porque são mais difíceis de instalar manualmente.

O arquivo baixado deve ter um nome parecido com:

```text
postgresql-18.x-...-windows-x64.exe
```

O número exato pode mudar, mas deve ser da linha 18 e para Windows 64 bits.

## Parte 3: instalar o PostgreSQL

Depois de baixar, abra o instalador.

### Tela Welcome

Clique em `Next`.

### Tela Installation Directory

Pode deixar a pasta que já aparece.

Normalmente será algo parecido com:

```text
C:\Program Files\PostgreSQL\18
```

Clique em `Next`.

### Tela Select Components

Essa tela é importante.

Deixe marcados:

- `PostgreSQL Server`
- `pgAdmin 4`
- `Command Line Tools`

Se aparecer `Stack Builder`, pode deixar marcado ou desmarcar. Ele não é necessário para o CoopManager.

Para uma instalação mais simples, deixe como o instalador já veio e siga em frente. Se no final abrir o Stack Builder, pode fechar ou cancelar.

Clique em `Next`.

### Tela Data Directory

Pode deixar a pasta sugerida.

Clique em `Next`.

### Tela Password

Aqui o instalador pede a senha do usuário administrador do PostgreSQL.

Esse usuário se chama:

```text
postgres
```

Crie uma senha e anote. Exemplo de senha apenas para entender o formato:

```text
Coop@2026Banco
```

Não use exatamente essa senha se o sistema for usado de verdade. Escolha uma senha própria e guarde em local seguro.

Digite a senha nos dois campos e clique em `Next`.

### Tela Port

Deixe o número:

```text
5432
```

Não mude essa porta, a menos que alguém técnico peça.

Clique em `Next`.

### Tela Advanced Options

Pode deixar como está.

Clique em `Next`.

### Tela Pre Installation Summary

Clique em `Next`.

### Tela Ready to Install

Clique em `Next` ou `Install`.

Aguarde a instalação terminar.

### Tela final

Se aparecer uma opção parecida com `Launch Stack Builder`, pode desmarcar antes de finalizar.

Se o Stack Builder abrir mesmo assim, pode fechar. Ele não é necessário para o CoopManager.

## Parte 4: reiniciar o computador

Depois de instalar o PostgreSQL, reinicie o computador principal.

Isso ajuda o Windows a reconhecer os comandos do PostgreSQL.

## Parte 5: criar o banco do CoopManager

Agora vamos criar o banco que o CoopManager vai usar.

No computador principal:

1. Abra a pasta do CoopManager.
2. Clique com o botão direito em uma área vazia da pasta.
3. Escolha `Abrir no Terminal` ou `Abrir no PowerShell`.
4. Copie e cole este comando:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/criar-banco-postgres-compartilhado.ps1 -HostName localhost -Database coopmanager -AppUser coopmanager -ConfigureThisComputer
```

5. Pressione Enter.

O programa vai pedir:

```text
Senha do administrador PostgreSQL (postgres)
```

Digite a senha que você criou durante a instalação do PostgreSQL.

Depois vai pedir:

```text
Senha que o CoopManager usará no banco (coopmanager)
```

Crie uma segunda senha. Essa senha é a que o CoopManager vai usar para entrar no banco.

Anote esta senha também.

Sugestão de anotação:

```text
Usuário do banco do CoopManager: coopmanager
Senha do banco do CoopManager: a senha escolhida
```

Se aparecer uma mensagem dizendo que foi configurado com sucesso, essa parte terminou.

## Parte 6: descobrir o IP do computador principal

Os outros computadores precisam saber o endereço do computador principal.

No computador principal:

1. Abra o menu Iniciar.
2. Digite `PowerShell`.
3. Abra o PowerShell.
4. Digite:

```powershell
ipconfig
```

5. Pressione Enter.
6. Procure uma linha chamada `Endereço IPv4` ou `IPv4 Address`.

O número será parecido com:

```text
192.168.1.25
```

Anote esse número.

Neste tutorial, quando aparecer `IP_DO_SERVIDOR`, troque pelo número que você encontrou.

Exemplo:

```text
IP_DO_SERVIDOR = 192.168.1.25
```

## Parte 7: liberar a conexão no firewall

Às vezes o Windows bloqueia a conexão dos outros computadores.

Para tentar liberar automaticamente:

1. No computador principal, abra a pasta do CoopManager.
2. Abra o PowerShell como administrador.
3. Cole este comando:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/criar-banco-postgres-compartilhado.ps1 -HostName localhost -Database coopmanager -AppUser coopmanager -ConfigureThisComputer -OpenFirewall
```

4. Pressione Enter.
5. Informe as senhas se o script pedir novamente.

Se não souber abrir o PowerShell como administrador:

1. Abra o menu Iniciar.
2. Digite `PowerShell`.
3. Clique com o botão direito.
4. Escolha `Executar como administrador`.

## Parte 8: configurar os outros computadores

Faça isso em cada computador que vai usar o CoopManager.

1. Instale o CoopManager normalmente.
2. Abra a pasta do CoopManager ou a pasta onde estão os scripts.
3. Abra o PowerShell nessa pasta.
4. Cole este comando, trocando `IP_DO_SERVIDOR` pelo IP do computador principal:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/configurar-banco-compartilhado.ps1 -HostName IP_DO_SERVIDOR -Port 5432 -Database coopmanager -User coopmanager
```

Exemplo, se o IP do computador principal for `192.168.1.25`:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/configurar-banco-compartilhado.ps1 -HostName 192.168.1.25 -Port 5432 -Database coopmanager -User coopmanager
```

O script vai pedir a senha do usuário `coopmanager`.

Digite a senha do banco do CoopManager que você anotou na Parte 5.

Depois abra o CoopManager normalmente.

## Parte 9: opção com tela visual

Se preferir configurar pela tela do aplicativo, também pode.

No computador que será configurado:

1. Abra o PowerShell na pasta do CoopManager.
2. Rode:

```powershell
CoopManager.exe --configurar-banco
```

3. Escolha a opção `PostgreSQL`.
4. Preencha:

```text
Servidor: IP do computador principal
Porta: 5432
Banco: coopmanager
Usuário: coopmanager
Senha: senha do usuário coopmanager
```

Exemplo:

```text
Servidor: 192.168.1.25
Porta: 5432
Banco: coopmanager
Usuário: coopmanager
Senha: a senha anotada
```

5. Clique para testar.
6. Se o teste der certo, salve.
7. Feche e abra o CoopManager novamente.

## Parte 10: primeiro acesso no banco compartilhado

Se o banco estiver zerado, o CoopManager vai abrir a tela de primeiro acesso.

Nessa tela, crie o primeiro administrador.

Esse usuário será o primeiro login do sistema e poderá cadastrar os produtores, clientes, produtos e pedidos.

Importante: faça isso só uma vez. Depois que o primeiro administrador for criado, os outros computadores já verão a tela normal de login.

## Parte 11: testar se está funcionando

Antes de entregar para todos, teste com dois computadores.

No computador principal:

1. Abra o CoopManager.
2. Faça login.
3. Cadastre um produto de teste ou um produtor de teste.

No segundo computador:

1. Abra o CoopManager.
2. Faça login.
3. Veja se o produto ou produtor criado apareceu.

Agora teste ao contrário:

1. No segundo computador, crie uma informação de teste.
2. Volte ao computador principal.
3. Veja se a informação apareceu.

Se apareceu nos dois, o banco compartilhado está funcionando.

## Problemas comuns

### O comando diz que `psql` não foi encontrado

Isso normalmente significa que o Windows não encontrou as ferramentas do PostgreSQL.

Tente nesta ordem:

1. Reinicie o computador.
2. Tente o comando de novo.
3. Se continuar, abra o menu Iniciar e procure por `SQL Shell (psql)`.
4. Se o `SQL Shell (psql)` existir, o PostgreSQL foi instalado, mas o comando não entrou no PATH.
5. Nesse caso, peça ajuda técnica para adicionar a pasta `bin` do PostgreSQL ao PATH.

A pasta costuma ser parecida com:

```text
C:\Program Files\PostgreSQL\18\bin
```

### O outro computador não conecta

Confira:

- O computador principal está ligado?
- Os dois computadores estão no mesmo Wi-Fi ou na mesma rede?
- O IP está correto?
- A porta está como `5432`?
- A senha do usuário `coopmanager` está correta?
- O firewall foi liberado?
- O PostgreSQL está instalado no computador principal?

### Como ver se o PostgreSQL está rodando

No computador principal:

1. Abra o menu Iniciar.
2. Digite `Serviços`.
3. Abra o aplicativo `Serviços`.
4. Procure algo parecido com:

```text
postgresql-x64-18
```

5. Veja se está como `Em execução`.

Se estiver parado, clique com o botão direito e escolha `Iniciar`.

### Ainda não conecta

Se tudo acima estiver certo e ainda não funcionar, pode faltar uma configuração interna do PostgreSQL para aceitar conexões de outros computadores.

Essa parte envolve arquivos como:

```text
postgresql.conf
pg_hba.conf
```

Nessa etapa, o recomendado é pedir ajuda técnica.

Explique para a pessoa de TI:

```text
O CoopManager precisa que o PostgreSQL aceite conexão na rede local pela porta 5432 para o banco coopmanager e usuário coopmanager.
```

## Voltar um computador para banco local

Se quiser que um computador pare de usar o banco compartilhado e volte a usar dados só dele:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/configurar-banco-compartilhado.ps1 -Local
```

Isso não apaga o banco compartilhado.

Só faz aquele computador voltar a usar o arquivo local:

```text
%APPDATA%\CoopManager\coopmanager.dat
```

## Cuidados importantes

- Guarde as senhas em local seguro.
- Não compartilhe a senha do banco com quem não cuida do sistema.
- Faça backup do PostgreSQL com frequência.
- Não exponha a porta `5432` diretamente para a internet.
- Se o computador principal for formatado sem backup, os dados podem ser perdidos.
- O APK/mobile atual ainda usa armazenamento local do próprio celular e não sincroniza com o PostgreSQL.

## Resumo para falar com o cliente

O CoopManager pode ser usado em vários computadores, mas precisa de um computador principal guardando os dados. Nesse computador é instalado o PostgreSQL. Depois, os outros computadores são configurados para acessar o computador principal. No uso diário, a equipe só abre o CoopManager, entra com login e trabalha normalmente.
