# Checklist: rota até o Moon Lord (NeoForge 1.21.1)

Atualizado em 2026-10-06, branch `feat/moonlord-route`
(confluence + TerraEntity, nos forks de `gabrieloliveira001`).

Legenda: `[x]` feito · `[~]` parcial / placeholder · `[ ]` falta · 🧪 validado no servidor de teste

## Resumo

Todo o caminho WoF → Moon Lord agora existe e dá para jogar em sobrevivência:

```
Wall of Flesh → (Mechanical Eye / Worm / Skull) → Gêmeos, Destruidor, Prime
  → Bulbo da Plantera na selva → Plantera → Chave do Templo
  → Templo da Selva → Célula de Energia no Altar Lihzahrd → Golem
  → ritual de cultistas na Masmorra → Cultista Lunático
  → 4 Pilares Celestiais → Moon Lord
```

Arte (modelos e texturas) é **placeholder** gerado por script: funcional e legível, mas precisa de arte de verdade.
A lógica de luta (padrões de ataque, dano e equilíbrio) **só foi testada sem jogador**. Falta jogar de verdade.

---

## 0. Base de progressão
- [x] 🧪 `KillBoard` avança a fase: mecânicos (os 3) → `MECHANICAL_BOSSES`, Plantera → `PLANTERA`, Golem → `GOLEM`, Moon Lord → `MOON_LORD`
- [x] Fases só sobem (matar o Esqueletron no Hardmode não volta a fase)
- [x] Conversão do mundo para Hardmode só roda na primeira entrada no Hardmode
- [x] Removida entrada duplicada do Deerclops nas bags

## 1. Bosses mecânicos
- [x] 🧪 Mechanical Eye / Worm / Skull (bigorna do Hardmode, só à noite)
- [x] Spawn natural noturno (1/10 por noite no Hardmode, configurável)
- [x] 🧪 Bag e loot do Destruidor (Alma do Poder)
- [x] Canhão do Prime agora atira bombas explosivas; fogo azul do Destruidor
- [x] 🧪 **Bug corrigido:** os Gêmeos nunca contavam como derrotados (olhos sem dono) e o corpo invisível não sumia

## 2. Plantera
- [x] Bulbo nasce na grama da selva subterrânea depois dos 3 mecânicos; quebrar invoca a Plantera
- [x] 🧪 Bag e loot (Chave do Templo, Gancho de Espinhos, The Axe)
- [ ] A Plantera ainda usa o modelo placeholder do *Visual Neuron* (pré-existente)
- [ ] Armas da Plantera não existem no mod

## 3. Golem
- [x] 🧪 Templo da Selva: labirinto de tijolos Lihzahrd sob selvas, porta com chave, potes, baús com Células de Energia, câmara do altar
- [x] Altar Lihzahrd (inquebrável) + Célula de Energia → invoca o Golem
- [x] 🧪 Golem: corpo que salta, cabeça (bolas de fogo, lasers no expert) que se solta e voa quando "morre", 2 punhos que socam alternados
- [x] Lihzahrd (inimigo do templo)
- [x] 🧪 Bag (Casca de Besouro, Picksaw, Sun Stone, Eye of the Golem, Shiny Stone no expert)
- [ ] Armadilhas do templo (super dardo, lança, bola de espinhos) e Cobra Voadora
- [ ] Golem enfurecer fora do templo
- [ ] Armas do Golem e armadura de besouro

## 4. Cultista Lunático
- [x] Ritual (4 devotos + 2 arqueiros) aparece na entrada da Masmorra depois do Golem
- [x] 🧪 Matar o último devoto invoca o Cultista
- [x] Cultista: bolas de fogo, névoa de gelo, raios, luz ancestral, teleporte e ritual de clones
- [ ] Phantasm Dragon / Ancient Vision / Ancient Doom (expert)
- [ ] Drop do Manipulador Ancestral

## 5. Eventos Lunares
- [x] 🧪 Morte do Cultista inicia o evento: 4 pilares a ~160 blocos em cruz; coordenadas no chat e no log
- [x] 🧪 Escudo (100 em single / 150 em multi): só cai matando inimigos do pilar por perto
- [x] 8 inimigos (2 por pilar) usando bases de modelos existentes
- [x] 🧪 Pilar morto dropa fragmentos; os 4 mortos → "Impending doom approaches..." → Moon Lord em 1 minuto
- [x] Sigilo Celestial (12 de cada fragmento) — **provisoriamente na bigorna do Hardmode**
- [ ] **Manipulador Ancestral** (estação de criação) e receitas lunares (asas, picaretas e brocas lunares já existem sem receita)
- [ ] Roster completo de inimigos dos pilares (o Terraria tem ~17)
- [ ] Marcação dos pilares no mapa / integração com TheTrackers

## 6. Moon Lord
- [x] 🧪 Núcleo invulnerável até destruir a cabeça e as 2 mãos; cada parte destruída solta um True Eye of Cthulhu
- [x] Cabeça: Phantasmal Deathray (varre na direção do alvo) e leque de projéteis; mãos: rajadas, esferas e olhos teleguiados
- [x] 🧪 Bag (Luminita bruta, moedas, poções, Gravity Globe no expert) e fase `MOON_LORD` (mundo "graduado")
- [ ] Armas do Moon Lord (Meowmere, Star Wrath, Terrarian, etc.), Portal Gun, Moon Bite
- [ ] Skybox/efeitos de tela durante a luta

---

## Bugs pré-existentes encontrados
- [ ] **Servidor dedicado não inicia** (`Confluence-Magic-Lib`): `PlayerAttackingStatePacket` referencia `LocalPlayer`/`Minecraft`, e o verificador carrega classes de cliente no servidor. Corrigir na lib movendo `sendToServer()` para uma classe só de cliente. O upstream da lib já reescreveu esse código.
- [ ] Vida dos bosses é escalada por `jogadores × multiplicador − 1`; com 0 jogadores online (ex.: invocado por bloco de comando) a vida vira 1.
- [ ] A conversão para Hardmode deixa o servidor muito lento por um tempo (picos de ~1 s por tick no teste).

## Opcional (não bloqueia o Moon Lord)
- [ ] Queen Slime, Duke Fishron, Empress of Light
- [ ] Lua de Abóbora / Lua Gelada (classes são stubs `// todo`)
- [ ] Eclipse Solar, Invasão Pirata, Marciana, Old One's Army
- [~] Prime Ender Dragon (original): faltam varrida e bola de sopro

## Como testar
1. `runData` em TerraEntity e ConfluenceOtherworld (os recursos gerados não são versionados).
2. `runClient`, criar mundo e, em criativo, usar os spawn eggs / `/summon terra_entity:<boss>`.
3. `/confluence gamePhase get|set <FASE>` mostra/ajusta a fase; `/confluence gameEvent start confluence:lunar_events` inicia os pilares.
4. `/locate structure confluence:jungle_temple` acha o templo.
