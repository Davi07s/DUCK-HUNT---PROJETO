# DUCK HUNT — PROJETO

Projeto de jogo 2D inspirado em *Duck Hunt*, desenvolvido com foco em **Programação Orientada a Objetos**, gerenciamento de estados e mecânicas de penalidade/recompensa.

---

## 1. Objetivo do Sistema

O jogador deve controlar o fluxo de erros representado por uma variável de estado chamada **Barra de Tolerância**.  
O sistema encerra a execução da partida quando essa variável atinge seu valor máximo.

O gameplay é baseado em **classificação de entidades móveis** (patos) e decisões rápidas do jogador.

---

## 2. Mecânica Central — Barra de Tolerância

A barra de tolerância funciona como um **medidor de falhas acumuladas**.

### Estados possíveis:
- **Valor mínimo:** 0  
- **Valor máximo:** limite definido pelo sistema  
- **Condição de derrota:** valor atual ≥ valor máximo

### Alterações na barra:

| Evento | Variação |
|--------|----------|
| Pato comum escapa da tela | +1 |
| Pato raro abatido | −4 |
| Pato falso raro abatido | +4 |
| Pato inocente abatido | Game Over imediato |

A barra nunca deve assumir valores negativos (controle de limite inferior recomendado).

---

## 3. Classificação de Entidades (Patos)

Os patos são entidades que compartilham características comuns, mas possuem **comportamentos distintos baseados em tipo**.

| Tipo | Categoria | Efeito Sistêmico |
|------|-----------|------------------|
| Pato Comum | Alvo padrão | Penalidade se escapar |
| Pato Raro | Alvo bônus | Redução de penalidade |
| Pato Falso Raro | Armadilha | Aumento significativo de penalidade |
| Pato Inocente | Entidade proibida | Condição instantânea de derrota |

---

## 4. Regras Formais de Jogo

1. O sistema deve monitorar continuamente:
   - Colisões entre tiro e entidades
   - Saída de entidades da área visível

2. Condições de término:
   - Barra de tolerância cheia
   - Abate de pato inocente

3. O jogador deve:
   - Priorizar patos raros
   - Evitar patos inocentes
   - Minimizar fuga de patos comuns

4. O jogo possui **mecânica de confusão intencional**, pois patos falsos raros simulam visualmente patos raros.

---

## 5. Estrutura Conceitual do Código

O projeto utiliza conceitos clássicos de POO:

- **Herança:** Classe base para entidades do tipo Animal/Pato  
- **Polimorfismo:** Comportamento diferenciado no evento de abate  
- **Encapsulamento:** Controle interno da barra de tolerância  
- **Estados de jogo:** Rodando, Game Over  

---

## 6. Sistema de Eventos

O jogo é orientado a eventos:

| Evento | Ação disparada |
|-------|----------------|
| Tiro acerta entidade | Aplicar regra do tipo de pato |
| Entidade sai da tela | Verificar se é pato comum |
| Barra atinge limite | Encerrar partida |
| Pato inocente abatido | Encerrar partida imediatamente |

---

## 7. Objetivos de Aprendizado

- Modelagem de entidades em jogos
- Controle de estado global
- Gerenciamento de penalidades
- Detecção de colisão
- Separação de responsabilidades em classes

---

## 8. Execução

```bash
git clone https://github.com/Davi07s/DUCK-HUNT---PROJETO.git
```

Abrir em uma IDE Java e executar a classe principal.

---

## Autor

Davi de Souza Santos Barbosa
