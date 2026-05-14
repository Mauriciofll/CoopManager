# Sistema de Gestão Cooperativa

Protótipo simples em Java para a matéria de extensão Projeto Empreendedor.

## Objetivo

Simular um sistema para ajudar Cassiano a atuar como intermediador entre produtores, comerciantes e clientes da associação.

## Funcionalidades

- Cadastro de produtos dos associados
- Catálogo digital simples
- Controle de estoque
- Criação de pedidos
- Atualização de status dos pedidos
- Apoio à logística de entrega ou retirada

## Como executar

Entre na pasta `src` e compile:

```bash
javac Main.java view/*.java controller/*.java repository/*.java model/*.java
java Main
```

## Status do pedido

- PENDENTE
- EM_SEPARACAO
- PRONTO
- FINALIZADO
