# Checklist: rota até o Moon Lord (NeoForge 1.21.1)

Análise de 2026-10-06.
- Confluence: branch `neoforge/1.21.1` @ `5d22de974`
- TerraEntity: branch `neoforge-dev/1.21.1` @ `62984cf3` (o submódulo está fixado em `c1102a4`, 4 commits atrás)

Legenda: `[x]` feito · `[~]` parcial · `[ ]` falta

## Resumo

| Etapa | Situação |
|---|---|
| Pré-Hardmode (King Slime → Wall of Flesh / Hill of Flesh) | Completo |
| Bosses mecânicos (Twins, Destroyer, Prime) | Entidades prontas, **sem invocação no survival** |
| Plantera | Entidade pronta, **sem bulbo, sem loot** |
| Golem | Só blocos decorativos Lihzahrd |
| Lunatic Cultist | Nada |
| Eventos Lunares (4 pilares) | Nada |
| Moon Lord | Nada |

- Bosses obrigatórios entre WoF e Moon Lord: 7 (Twins, Destroyer, Prime, Plantera, Golem, Cultist, Moon Lord)
- Com entidade implementada: 4 de 7
- Alcançáveis jogando no survival: **0 de 7** (hoje só existem via spawn egg)
- Estimativa grosseira (por esforço) da rota WoF → Moon Lord: **~25–30% feita**

---

## 0. Base de progressão (bloqueia tudo)

- [ ] `KillBoard.defeat()` avançar as fases `MECHANICAL_BOSSES` (3 mecânicos mortos), `PLANTERA`, `GOLEM` e `MOON_LORD`.
      Hoje só avança até `WALL_OF_FLESH` ([KillBoard.java:94-100](../src/main/java/org/confluence/mod/common/data/saved/KillBoard.java#L94-L100)).
      As fases existem em `GamePhase`, mas nada as ativa.
- [ ] `KillBoard.setGamePhase()`: só chamar `onUnlockHardmode` + `HardmodeConvertor.start` ao **entrar** no Hardmode.
      Hoje qualquer fase ≥ WoF dispara a conversão de novo, então ela rodaria em cada fase nova
      ([KillBoard.java:119-124](../src/main/java/org/confluence/mod/common/data/saved/KillBoard.java#L119-L124)).
- [ ] Atualizar o ponteiro do submódulo TerraEntity (`c1102a4` → `62984cf3`, inclui `修复BOSS召唤问题`).
- [ ] (menor) `TreasureBagSubProvider` registra `DEERCLOPS` duas vezes
      ([TreasureBagSubProvider.java:18-20](../src/main/java/org/confluence/mod/common/data/gen/data_map/TreasureBagSubProvider.java#L18-L20)).

## 1. Bosses mecânicos

### The Twins
- [x] Entidade e IA (Retinazer + Spazmatism)
- [x] Treasure bag + loot (Soul of Sight, Hallowed Ingot)
- [ ] Item **Mechanical Eye** (receita + `BossSummoningItem`, só à noite)
- [ ] Spawn natural noturno no Hardmode

### The Destroyer
- [~] Entidade, IA, sondas e laser. TODO: ataque de fogo azul (`TerraEntity/.../thedestroyer/TheDestroyerPart.java:131`)
- [ ] Treasure bag (não existe em `TreasureBagItems`)
- [ ] Loot com **Soul of Might**: hoje não existe nenhuma fonte de Soul of Might no jogo
- [ ] Item **Mechanical Worm**
- [ ] Spawn natural noturno

### Skeletron Prime
- [~] Entidade e IA. TODO: projétil do canhão (`TerraEntity/.../skeletronprime/SkeletronPrimePart.java:260`)
- [x] Treasure bag + loot (Soul of Fright, Hallowed Ingot)
- [ ] Item **Mechanical Skull**
- [ ] Spawn natural noturno

### Integração
- [ ] Fase `MECHANICAL_BOSSES` ao derrotar os três
- [ ] Conferir as receitas que usam Soul of Might/Sight/Fright e Hallowed Ingot

## 2. Plantera

- [x] Entidade e IA (ganchos, tentáculos)
- [ ] Bloco **Bulbo da Plantera** gerado na selva subterrânea depois dos 3 mecânicos
- [ ] Treasure bag + loot (inclui a **Temple Key**: o item já existe e `LihzahrdDoorBlock` já aceita a chave)
- [ ] Fase `PLANTERA`

## 3. Golem

- [~] Blocos Lihzahrd (tijolos, porta com chave, estátuas, pote) já existem
- [ ] Estrutura do **Templo da Selva** no worldgen (não há structure nem feature)
- [ ] Bloco **Lihzahrd Altar** + item **Lihzahrd Power Cell**
- [ ] Inimigos do templo (Lihzahrd, Flying Snake) + armadilhas (Super Dart, Spiky Ball, Flame, Spear)
- [ ] Entidade **Golem** (corpo, cabeça, 2 punhos) + IA
- [ ] Treasure bag + loot (Beetle Husk não existe; Picksaw já existe)
- [ ] Fase `GOLEM`

## 4. Lunatic Cultist

- [ ] Cultistas na entrada da Masmorra depois do Golem
- [ ] Entidade **Lunatic Cultist** + IA (clones, Fireball, Ice Mist, Lightning, Phantasm Dragon, Ancient Light)
- [ ] Loot (Ancient Manipulator)
- [ ] A morte dele inicia os Eventos Lunares

## 5. Eventos Lunares

- [ ] `GameEvent` dos pilares (escudo com contador de kills, término quando os 4 caem)
- [ ] 4 **Pilares Celestiais**: Solar, Vortex, Nebula, Stardust
- [ ] Inimigos de cada pilar (cerca de 15–17 tipos no total)
- [ ] **Fragmentos** Solar/Vortex/Nebula/Stardust (não existem)
- [ ] Bloco **Ancient Manipulator** + receitas lunares (os itens lunares já existem: asas, picaretas, brocas, hamaxes)
- [ ] **Celestial Sigil** (invocação alternativa)

## 6. Moon Lord (chefão final)

- [ ] Entidade multi-parte (cabeça, 2 mãos, núcleo, True Eyes) + IA (Phantasmal Deathray, Bolt, Sphere, Eye)
- [ ] Debuff Moon Bite + céu/ambiente durante a luta
- [ ] Treasure bag + loot (Luminite hoje só vem por shimmer de `raw_chlorophyte`; Meowmere já existe; falta Portal Gun etc.)
- [ ] Fase `MOON_LORD` → flag `GRADUATED` (o tratamento já existe em `setGamePhase`)

---

## Opcional (não bloqueia o Moon Lord)

- [ ] Queen Slime (o bloco Gelatin Crystal já existe)
- [ ] Duke Fishron (Truffle Worm e Fishron Wings já existem)
- [ ] Empress of Light (a entidade Prismatic Lacewing e as Empress Wings já existem)
- [ ] Lua de Abóbora / Lua Gelada: as classes são stubs vazios marcados `// todo`
      ([PumpkinMoonGameEvent.java](../src/main/java/org/confluence/mod/common/gameevent/PumpkinMoonGameEvent.java),
      [FrostMoonGameEvent.java](../src/main/java/org/confluence/mod/common/gameevent/FrostMoonGameEvent.java))
- [ ] Eclipse Solar, Invasão Pirata, Invasão Marciana, Old One's Army
- [~] Prime Ender Dragon (boss original). TODOs: varrida e bola de sopro de dragão (`TerraEntity/.../primeenderdragon/PrimeEnderDragon.java:219,228`)
- [ ] `HillOfFlesh.java:315`: `setCdReduce(0.9f); // todo debug` ainda ativo
