(function () {
  "use strict";

  const ROAD_WIDTH = 10.5;
  const HALF_ROAD = 4.35;
  const LANES = [-3.2, 0, 3.2];

  const STAGES = [
    {
      name: "Jakarta - Monas",
      skyColor: 0xff8a4c,
      fogColor: 0xf47a42,
      ambientColor: 0xffe0b2,
      sunColor: 0xffd180,
      batikPrimary: "#9e1b1b",
      batikAccent: "#ffb300"
    },
    {
      name: "Yogyakarta - Prambanan",
      skyColor: 0x241744,
      fogColor: 0x2e1c52,
      ambientColor: 0xc5cae9,
      sunColor: 0xffab40,
      batikPrimary: "#3e2723",
      batikAccent: "#ffd54f"
    },
    {
      name: "Bali - Pesisir Dewata",
      skyColor: 0x26a69a,
      fogColor: 0x4db6ac,
      ambientColor: 0xe0f7fa,
      sunColor: 0xfff59d,
      batikPrimary: "#00695c",
      batikAccent: "#ffca28"
    }
  ];

  const audio = new window.NusantaraAudio();

  const state = {
    mode: "menu", // "menu" | "playing" | "paused" | "gameover"
    stage: 0,
    score: 0,
    coins: 0,
    distance: 0,
    combo: 0,
    comboTimer: 0,
    turboCharge: 35,
    turboTimer: 0,
    hasShield: false,
    magnetTimer: 0,
    speed: 24,
    baseSpeed: 24,
    playerX: 0,
    playerY: 0,
    playerVelY: 0,
    isGrounded: true,
    cameraMode: 0, // 0: Chase, 1: High Arcade, 2: Dynamic Close
    mouseSteerEnabled: true,
    activeInput: "touch",
    lastHudSync: 0,
    invulnTimer: 0,
    shakeTimer: 0
  };

  const input = {
    left: false,
    right: false,
    jumpQueued: false,
    duck: false,
    turboQueued: false,
    mouseTargetX: null,
    mouseActiveUntil: 0,
    touchDragActive: false,
    touchStartX: 0,
    touchStartY: 0,
    touchLastX: 0,
    touchMoved: false
  };

  // Three.js core objects
  const canvas = document.getElementById("game-canvas");
  const renderer = new THREE.WebGLRenderer({ canvas: canvas, antialias: true, powerPreference: "high-performance" });
  renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
  renderer.setSize(window.innerWidth, window.innerHeight);
  renderer.shadowMap.enabled = true;
  renderer.shadowMap.type = THREE.PCFSoftShadowMap;

  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(58, window.innerWidth / window.innerHeight, 0.1, 260);
  camera.position.set(0, 4.4, 8.6);

  const ambientLight = new THREE.AmbientLight(0xffe0b2, 0.72);
  scene.add(ambientLight);

  const dirLight = new THREE.DirectionalLight(0xffd180, 1.15);
  dirLight.position.set(14, 24, 10);
  dirLight.castShadow = true;
  dirLight.shadow.mapSize.width = 1024;
  dirLight.shadow.mapSize.height = 1024;
  dirLight.shadow.camera.near = 2;
  dirLight.shadow.camera.far = 90;
  const d = 22;
  dirLight.shadow.camera.left = -d;
  dirLight.shadow.camera.right = d;
  dirLight.shadow.camera.top = d;
  dirLight.shadow.camera.bottom = -d;
  scene.add(dirLight);

  const hemiLight = new THREE.HemisphereLight(0xffe8c2, 0x263238, 0.45);
  scene.add(hemiLight);

  // Environment meshes
  let roadMesh, groundMesh, oceanMesh, landmarkGroup;
  let roadTexture, groundTexture;
  const propsPool = [];
  const obstacles = [];
  const collectibles = [];
  const particles = [];
  let spawnObstacleTimer = 0;
  let spawnCoinTimer = 0;

  // Build Player Bajaj 3D
  const bajaj = createBajajModel();
  scene.add(bajaj.group);

  function createBajajModel() {
    const group = new THREE.Group();

    const bodyMat = new THREE.MeshStandardMaterial({ color: 0xff6d00, roughness: 0.35, metalness: 0.25 });
    const creamMat = new THREE.MeshStandardMaterial({ color: 0xfff3e0, roughness: 0.45 });
    const roofMat = new THREE.MeshStandardMaterial({ color: 0x263238, roughness: 0.75 });
    const chromeMat = new THREE.MeshStandardMaterial({ color: 0xe0e0e0, roughness: 0.2, metalness: 0.8 });
    const tireMat = new THREE.MeshStandardMaterial({ color: 0x181818, roughness: 0.85 });
    const glassMat = new THREE.MeshStandardMaterial({ color: 0x80deea, roughness: 0.1, metalness: 0.2, transparent: true, opacity: 0.75 });

    // Lower cabin chassis
    const lowerBody = new THREE.Mesh(new THREE.BoxGeometry(1.52, 0.72, 2.25), bodyMat);
    lowerBody.position.set(0, 0.62, 0.1);
    lowerBody.castShadow = true;
    lowerBody.receiveShadow = true;
    group.add(lowerBody);

    // Tapered front nose
    const frontNose = new THREE.Mesh(new THREE.CylinderGeometry(0.42, 0.68, 0.85, 8), bodyMat);
    frontNose.position.set(0, 0.68, -1.18);
    frontNose.castShadow = true;
    group.add(frontNose);

    // Cream racing stripe & side trim
    const trim = new THREE.Mesh(new THREE.BoxGeometry(1.56, 0.18, 2.0), creamMat);
    trim.position.set(0, 0.62, 0.12);
    group.add(trim);

    // Windshield frame & glass
    const windshield = new THREE.Mesh(new THREE.BoxGeometry(1.16, 0.56, 0.08), glassMat);
    windshield.position.set(0, 1.24, -0.78);
    windshield.rotation.x = 0.28;
    group.add(windshield);

    // Pillars & Canvas Roof Canopy
    const roof = new THREE.Mesh(new THREE.BoxGeometry(1.48, 0.14, 1.96), roofMat);
    roof.position.set(0, 1.56, 0.16);
    roof.castShadow = true;
    group.add(roof);

    const backWall = new THREE.Mesh(new THREE.BoxGeometry(1.44, 0.62, 0.1), roofMat);
    backWall.position.set(0, 1.24, 1.12);
    group.add(backWall);

    // Stylized Driver inside
    const driverTorso = new THREE.Mesh(new THREE.CylinderGeometry(0.22, 0.26, 0.5, 8), new THREE.MeshStandardMaterial({ color: 0x00897b, roughness: 0.6 }));
    driverTorso.position.set(0, 1.1, -0.2);
    group.add(driverTorso);

    const driverHelmet = new THREE.Mesh(new THREE.SphereGeometry(0.2, 10, 10), new THREE.MeshStandardMaterial({ color: 0xffca28, roughness: 0.3 }));
    driverHelmet.position.set(0, 1.44, -0.2);
    group.add(driverHelmet);

    // Headlight & Tail lights
    const headLight = new THREE.Mesh(new THREE.CylinderGeometry(0.14, 0.14, 0.1, 12), new THREE.MeshBasicMaterial({ color: 0xfff59d }));
    headLight.rotation.x = Math.PI / 2;
    headLight.position.set(0, 0.84, -1.62);
    group.add(headLight);

    const tailMat = new THREE.MeshBasicMaterial({ color: 0xff1744 });
    const tailL = new THREE.Mesh(new THREE.BoxGeometry(0.2, 0.12, 0.08), tailMat);
    tailL.position.set(-0.54, 0.64, 1.24);
    const tailR = tailL.clone();
    tailR.position.x = 0.54;
    group.add(tailL, tailR);

    // Exhaust pipe
    const exhaust = new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.07, 0.45, 8), chromeMat);
    exhaust.rotation.x = Math.PI / 2;
    exhaust.position.set(0.45, 0.32, 1.3);
    group.add(exhaust);

    // Three wheels (1 front steerable, 2 rear)
    function makeWheel() {
      const wGroup = new THREE.Group();
      const tire = new THREE.Mesh(new THREE.CylinderGeometry(0.32, 0.32, 0.22, 16), tireMat);
      tire.rotation.z = Math.PI / 2;
      tire.castShadow = true;
      const hub = new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.15, 0.24, 10), chromeMat);
      hub.rotation.z = Math.PI / 2;
      wGroup.add(tire, hub);
      return wGroup;
    }

    const frontWheel = makeWheel();
    frontWheel.position.set(0, 0.32, -1.25);
    const rearL = makeWheel();
    rearL.position.set(-0.78, 0.32, 0.78);
    const rearR = makeWheel();
    rearR.position.set(0.78, 0.32, 0.78);
    group.add(frontWheel, rearL, rearR);

    // Perisai Keris Shield Bubble
    const shieldSphere = new THREE.Mesh(
      new THREE.SphereGeometry(1.72, 20, 16),
      new THREE.MeshBasicMaterial({ color: 0xffd54f, transparent: true, opacity: 0.26, wireframe: true })
    );
    shieldSphere.position.set(0, 0.9, 0);
    shieldSphere.visible = false;
    group.add(shieldSphere);

    // Turbo Flame Glow
    const turboGlow = new THREE.Mesh(
      new THREE.ConeGeometry(0.28, 1.15, 10),
      new THREE.MeshBasicMaterial({ color: 0x00e5ff, transparent: true, opacity: 0.85 })
    );
    turboGlow.rotation.x = Math.PI / 2;
    turboGlow.position.set(0.45, 0.32, 2.0);
    turboGlow.visible = false;
    group.add(turboGlow);

    return {
      group: group,
      frontWheel: frontWheel,
      rearL: rearL,
      rearR: rearR,
      shieldSphere: shieldSphere,
      turboGlow: turboGlow
    };
  }

  function setupEnvironment(stageIndex) {
    const cfg = STAGES[stageIndex] || STAGES[0];
    scene.background = new THREE.Color(cfg.skyColor);
    scene.fog = new THREE.FogExp2(cfg.fogColor, 0.0115);
    ambientLight.color.setHex(cfg.ambientColor);
    dirLight.color.setHex(cfg.sunColor);

    if (roadMesh) scene.remove(roadMesh);
    if (groundMesh) scene.remove(groundMesh);
    if (oceanMesh) scene.remove(oceanMesh);
    if (landmarkGroup) scene.remove(landmarkGroup);

    roadTexture = window.NusantaraTextures.makeAsphaltTexture(stageIndex);
    roadMesh = new THREE.Mesh(
      new THREE.PlaneGeometry(ROAD_WIDTH, 260),
      new THREE.MeshStandardMaterial({ map: roadTexture, roughness: 0.85 })
    );
    roadMesh.rotation.x = -Math.PI / 2;
    roadMesh.position.set(0, 0, -90);
    roadMesh.receiveShadow = true;
    scene.add(roadMesh);

    groundTexture = window.NusantaraTextures.makeGroundTexture(stageIndex);
    groundMesh = new THREE.Mesh(
      new THREE.PlaneGeometry(240, 260),
      new THREE.MeshStandardMaterial({ map: groundTexture, roughness: 0.95 })
    );
    groundMesh.rotation.x = -Math.PI / 2;
    groundMesh.position.set(0, -0.05, -90);
    groundMesh.receiveShadow = true;
    scene.add(groundMesh);

    // Bali coastal ocean strip
    if (stageIndex === 2) {
      oceanMesh = new THREE.Mesh(
        new THREE.PlaneGeometry(110, 260),
        new THREE.MeshStandardMaterial({ color: 0x00838f, roughness: 0.15, metalness: 0.45 })
      );
      oceanMesh.rotation.x = -Math.PI / 2;
      oceanMesh.position.set(66, -0.02, -90);
      scene.add(oceanMesh);
    }

    // Distant iconic Indonesian Landmark on the horizon
    landmarkGroup = buildHorizonLandmark(stageIndex, cfg);
    scene.add(landmarkGroup);

    // Clear & rebuild scrolling roadside props
    propsPool.forEach(function (p) { scene.remove(p); });
    propsPool.length = 0;

    for (let z = 12; z >= -160; z -= 14) {
      spawnRoadsidePair(z, stageIndex, cfg);
    }
  }

  function buildHorizonLandmark(stageIndex, cfg) {
    const grp = new THREE.Group();
    if (stageIndex === 0) {
      // Jakarta Monas Monument
      const base = new THREE.Mesh(new THREE.BoxGeometry(16, 5, 16), new THREE.MeshStandardMaterial({ color: 0xeceff1 }));
      base.position.set(-14, 2.5, -135);
      const deck = new THREE.Mesh(new THREE.BoxGeometry(10, 2.5, 10), new THREE.MeshStandardMaterial({ color: 0xf5f5f5 }));
      deck.position.set(-14, 10, -135);
      const obelisk = new THREE.Mesh(new THREE.CylinderGeometry(1.4, 2.8, 36, 4), new THREE.MeshStandardMaterial({ color: 0xffffff }));
      obelisk.rotation.y = Math.PI / 4;
      obelisk.position.set(-14, 23, -135);
      const flame = new THREE.Mesh(new THREE.ConeGeometry(1.8, 5.5, 8), new THREE.MeshBasicMaterial({ color: 0xffd700 }));
      flame.position.set(-14, 43.5, -135);
      grp.add(base, deck, obelisk, flame);
    } else if (stageIndex === 1) {
      // Candi Prambanan Silhouette Towers
      const stoneMat = new THREE.MeshStandardMaterial({ color: 0x4e4351, roughness: 0.9 });
      [-18, 0, 18].forEach(function (xPos, idx) {
        const heightScale = idx === 1 ? 1.35 : 1.0;
        for (let tier = 0; tier < 5; tier++) {
          const w = (7 - tier * 1.15) * heightScale;
          const h = 4.2 * heightScale;
          const box = new THREE.Mesh(new THREE.BoxGeometry(w, h, w), stoneMat);
          box.position.set(xPos, tier * h + h * 0.5, -138);
          grp.add(box);
        }
      });
    } else {
      // Bali Sun & Coastal Mountains
      const sun = new THREE.Mesh(new THREE.CircleGeometry(14, 24), new THREE.MeshBasicMaterial({ color: 0xffeb3b }));
      sun.position.set(22, 22, -155);
      const volcano = new THREE.Mesh(new THREE.ConeGeometry(34, 30, 7), new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 0.9 }));
      volcano.position.set(-28, 14, -148);
      grp.add(sun, volcano);
    }
    return grp;
  }

  function createPalmTree() {
    const tree = new THREE.Group();
    const trunk = new THREE.Mesh(new THREE.CylinderGeometry(0.18, 0.32, 4.4, 7), new THREE.MeshStandardMaterial({ color: 0x6d4c41 }));
    trunk.position.y = 2.2;
    trunk.castShadow = true;
    tree.add(trunk);

    const leafMat = new THREE.MeshStandardMaterial({ color: 0x2e7d32, roughness: 0.7 });
    for (let i = 0; i < 6; i++) {
      const leaf = new THREE.Mesh(new THREE.ConeGeometry(0.65, 2.8, 5), leafMat);
      leaf.position.y = 4.35;
      leaf.rotation.z = 1.05;
      leaf.rotation.y = (i / 6) * Math.PI * 2;
      tree.add(leaf);
    }
    return tree;
  }

  function createWarungStall(batikTex) {
    const grp = new THREE.Group();
    const base = new THREE.Mesh(new THREE.BoxGeometry(2.6, 1.2, 2.2), new THREE.MeshStandardMaterial({ color: 0x5d4037 }));
    base.position.y = 0.6;
    const roof = new THREE.Mesh(new THREE.BoxGeometry(3.0, 0.25, 2.6), new THREE.MeshStandardMaterial({ map: batikTex }));
    roof.position.y = 2.25;
    const lantern = new THREE.Mesh(new THREE.SphereGeometry(0.22, 8, 8), new THREE.MeshBasicMaterial({ color: 0xffd54f }));
    lantern.position.set(0, 1.8, 1.1);
    grp.add(base, roof, lantern);
    return grp;
  }

  function createCandiBentarPair() {
    const gate = new THREE.Group();
    const stoneMat = new THREE.MeshStandardMaterial({ color: 0x8d6e63, roughness: 0.85 });
    [-6.6, 6.6].forEach(function (xPos) {
      const pillar = new THREE.Mesh(new THREE.BoxGeometry(1.5, 5.2, 1.5), stoneMat);
      pillar.position.set(xPos, 2.6, 0);
      const top = new THREE.Mesh(new THREE.ConeGeometry(1.1, 2.2, 4), stoneMat);
      top.rotation.y = Math.PI / 4;
      top.position.set(xPos, 6.2, 0);
      gate.add(pillar, top);
    });
    return gate;
  }

  function spawnRoadsidePair(z, stageIndex, cfg) {
    const batikTex = window.NusantaraTextures.makeBatikTexture(cfg.batikPrimary, cfg.batikAccent);

    const leftProp = (Math.abs(Math.round(z)) % 28 === 0) ? createWarungStall(batikTex) : createPalmTree();
    leftProp.position.set(-7.6 - Math.random() * 1.4, 0, z);
    scene.add(leftProp);
    propsPool.push(leftProp);

    const rightProp = createPalmTree();
    rightProp.position.set(7.6 + Math.random() * 1.4, 0, z);
    scene.add(rightProp);
    propsPool.push(rightProp);

    if (stageIndex >= 1 && Math.abs(Math.round(z)) % 42 === 0) {
      const gate = createCandiBentarPair();
      gate.position.set(0, 0, z);
      scene.add(gate);
      propsPool.push(gate);
    }
  }

  function spawnObstacle() {
    const lane = LANES[Math.floor(Math.random() * LANES.length)];
    const typeRoll = Math.random();
    const obsGroup = new THREE.Group();
    let halfW = 0.85;
    let height = 1.4;
    let jumpable = false;

    if (typeRoll < 0.34) {
      // Angkot / Minibus
      const color = state.stage === 0 ? 0x0288d1 : (state.stage === 1 ? 0x5d4037 : 0x00897b);
      const body = new THREE.Mesh(new THREE.BoxGeometry(1.85, 1.45, 3.2), new THREE.MeshStandardMaterial({ color: color, roughness: 0.4 }));
      body.position.y = 0.92;
      body.castShadow = true;
      const win = new THREE.Mesh(new THREE.BoxGeometry(1.9, 0.45, 2.2), new THREE.MeshStandardMaterial({ color: 0xb2ebf2 }));
      win.position.y = 1.2;
      obsGroup.add(body, win);
      halfW = 0.95;
      height = 1.75;
    } else if (typeRoll < 0.68) {
      // Gerobak Bakso / Angkringan
      const cart = new THREE.Mesh(new THREE.BoxGeometry(1.45, 1.05, 1.8), new THREE.MeshStandardMaterial({ color: 0x8d6e63, roughness: 0.7 }));
      cart.position.y = 0.62;
      cart.castShadow = true;
      const glass = new THREE.Mesh(new THREE.BoxGeometry(1.3, 0.55, 1.2), new THREE.MeshStandardMaterial({ color: 0xe0f7fa, transparent: true, opacity: 0.65 }));
      glass.position.y = 1.4;
      const umbrella = new THREE.Mesh(new THREE.ConeGeometry(1.15, 0.5, 8), new THREE.MeshStandardMaterial({ color: 0xd32f2f }));
      umbrella.position.y = 2.1;
      obsGroup.add(cart, glass, umbrella);
      halfW = 0.8;
      height = 1.6;
    } else {
      // Low Road Barrier / Pembatas Jalan (Jumpable!)
      const bar = new THREE.Mesh(new THREE.BoxGeometry(2.1, 0.68, 0.65), new THREE.MeshStandardMaterial({ color: 0xff9100, roughness: 0.5 }));
      bar.position.y = 0.34;
      bar.castShadow = true;
      const stripe = new THREE.Mesh(new THREE.BoxGeometry(2.14, 0.2, 0.68), new THREE.MeshStandardMaterial({ color: 0xffffff }));
      stripe.position.y = 0.44;
      obsGroup.add(bar, stripe);
      halfW = 1.0;
      height = 0.75;
      jumpable = true;
    }

    obsGroup.position.set(lane, 0, -130);
    scene.add(obsGroup);
    obstacles.push({
      mesh: obsGroup,
      x: lane,
      z: -130,
      halfW: halfW,
      height: height,
      jumpable: jumpable,
      passed: false
    });
  }

  function spawnCollectible() {
    const lane = LANES[Math.floor(Math.random() * LANES.length)];
    const roll = Math.random();
    let kind = "coin";
    let mesh;

    if (roll < 0.78) {
      kind = "coin";
      mesh = new THREE.Mesh(
        new THREE.CylinderGeometry(0.45, 0.45, 0.14, 16),
        new THREE.MeshStandardMaterial({ color: 0xffd54f, emissive: 0xff8f00, emissiveIntensity: 0.3, metalness: 0.8, roughness: 0.2 })
      );
      mesh.rotation.x = Math.PI / 2;
    } else if (roll < 0.87) {
      kind = "shield"; // Perisai Keris
      mesh = new THREE.Mesh(
        new THREE.OctahedronGeometry(0.55, 0),
        new THREE.MeshStandardMaterial({ color: 0xffca28, emissive: 0xff6f00, emissiveIntensity: 0.45, metalness: 0.7, roughness: 0.15 })
      );
    } else if (roll < 0.94) {
      kind = "magnet"; // Magnet Rezeki
      mesh = new THREE.Mesh(
        new THREE.TorusGeometry(0.42, 0.14, 8, 16, Math.PI),
        new THREE.MeshStandardMaterial({ color: 0xe53935, emissive: 0xb71c1c, emissiveIntensity: 0.35 })
      );
    } else {
      kind = "nasi"; // Nasi Goreng Bonus
      mesh = new THREE.Mesh(
        new THREE.ConeGeometry(0.48, 0.65, 10),
        new THREE.MeshStandardMaterial({ color: 0xffa726, emissive: 0xe65100, emissiveIntensity: 0.25 })
      );
    }

    mesh.position.set(lane, 0.95, -125);
    scene.add(mesh);
    collectibles.push({ mesh: mesh, kind: kind, x: lane, z: -125 });
  }

  function emitExhaustParticle(isTurbo) {
    const size = isTurbo ? 0.22 : 0.14;
    const color = isTurbo ? 0x00e5ff : 0xcfd8dc;
    const p = new THREE.Mesh(
      new THREE.SphereGeometry(size, 6, 6),
      new THREE.MeshBasicMaterial({ color: color, transparent: true, opacity: 0.65 })
    );
    p.position.set(state.playerX + 0.45, state.playerY + 0.34, 1.55);
    scene.add(p);
    particles.push({ mesh: p, life: 0.45, vx: (Math.random() - 0.5) * 0.6, vy: 0.8 + Math.random() * 0.6, vz: 5 });
  }

  function clearEntities() {
    obstacles.forEach(function (o) { scene.remove(o.mesh); });
    obstacles.length = 0;
    collectibles.forEach(function (c) { scene.remove(c.mesh); });
    collectibles.length = 0;
    particles.forEach(function (p) { scene.remove(p.mesh); });
    particles.length = 0;
  }

  function resetRun(stageIdx) {
    if (typeof stageIdx === "number" && stageIdx !== state.stage) {
      state.stage = stageIdx;
      setupEnvironment(state.stage);
    }
    clearEntities();
    state.score = 0;
    state.coins = 0;
    state.distance = 0;
    state.combo = 0;
    state.comboTimer = 0;
    state.turboCharge = 40;
    state.turboTimer = 0;
    state.hasShield = false;
    state.magnetTimer = 0;
    state.speed = 24;
    state.baseSpeed = 24;
    state.playerX = 0;
    state.playerY = 0;
    state.playerVelY = 0;
    state.isGrounded = true;
    state.invulnTimer = 0;
    state.shakeTimer = 0;
    spawnObstacleTimer = 1.0;
    spawnCoinTimer = 0.4;
    state.mode = "playing";
    audio.ensureContext();
    syncHud(true);
  }

  function triggerJump() {
    if (state.mode !== "playing") return;
    if (state.isGrounded) {
      state.playerVelY = 12.2;
      state.isGrounded = false;
      audio.playJump();
    }
  }

  function triggerTurbo() {
    if (state.mode !== "playing") return;
    if (state.turboCharge >= 100 && state.turboTimer <= 0) {
      state.turboCharge = 0;
      state.turboTimer = 4.2;
      audio.playKlaksonTurbo();
    }
  }

  function notifyBridge(method, payload) {
    if (window.AndroidBridge && typeof window.AndroidBridge[method] === "function") {
      try {
        window.AndroidBridge[method](payload);
      } catch (e) {}
    }
  }

  function syncHud(force) {
    const now = performance.now();
    if (!force && now - state.lastHudSync < 80) return;
    state.lastHudSync = now;
    const payload = JSON.stringify({
      mode: state.mode,
      stage: state.stage,
      score: Math.floor(state.score),
      coins: state.coins,
      distance: Math.floor(state.distance),
      combo: state.combo,
      turboCharge: Math.floor(state.turboCharge),
      turboActive: state.turboTimer > 0,
      hasShield: state.hasShield,
      magnetSeconds: Math.ceil(state.magnetTimer),
      speedKmh: Math.round(state.speed * 3.1),
      activeInput: state.activeInput,
      cameraMode: state.cameraMode
    });
    notifyBridge("onHudUpdate", payload);
  }

  // Input handlers: Keyboard, Mouse, Touch
  function handleKeyCode(code, isDown) {
    if (isDown) state.activeInput = "keyboard";
    switch (code) {
      case "ArrowLeft":
      case "KeyA":
        input.left = isDown;
        input.mouseTargetX = null;
        break;
      case "ArrowRight":
      case "KeyD":
        input.right = isDown;
        input.mouseTargetX = null;
        break;
      case "ArrowUp":
      case "KeyW":
      case "Space":
        if (isDown) triggerJump();
        break;
      case "ArrowDown":
      case "KeyS":
        input.duck = isDown;
        if (isDown && !state.isGrounded) state.playerVelY = Math.min(state.playerVelY, -14);
        break;
      case "ShiftLeft":
      case "ShiftRight":
      case "KeyE":
        if (isDown) triggerTurbo();
        break;
      case "KeyC":
        if (isDown) state.cameraMode = (state.cameraMode + 1) % 3;
        break;
      case "Escape":
      case "KeyP":
        if (isDown && state.mode === "playing") {
          state.mode = "paused";
          notifyBridge("onPauseRequested", "");
          syncHud(true);
        }
        break;
    }
  }

  window.addEventListener("keydown", function (e) {
    if (["ArrowLeft", "ArrowRight", "ArrowUp", "ArrowDown", "Space"].indexOf(e.code) !== -1) {
      e.preventDefault();
    }
    handleKeyCode(e.code, true);
  });

  window.addEventListener("keyup", function (e) {
    handleKeyCode(e.code, false);
  });

  // Mouse controls (OTG / USB / PC)
  canvas.addEventListener("mousemove", function (e) {
    if (!state.mouseSteerEnabled || state.mode !== "playing") return;
    // Ignore synthetic mouse events right after touch
    if (performance.now() - input.touchLastTime < 500) return;
    state.activeInput = "mouse";
    const normX = (e.clientX / Math.max(1, window.innerWidth)) * 2 - 1;
    input.mouseTargetX = Math.max(-HALF_ROAD, Math.min(HALF_ROAD, normX * (HALF_ROAD + 0.8)));
    input.mouseActiveUntil = performance.now() + 2500;
  });

  canvas.addEventListener("mousedown", function (e) {
    if (e.button === 0) {
      state.activeInput = "mouse";
      if (state.mode === "playing") triggerJump();
    } else if (e.button === 2) {
      state.activeInput = "mouse";
      if (state.mode === "playing") triggerTurbo();
    }
  });

  canvas.addEventListener("contextmenu", function (e) {
    e.preventDefault();
  });

  canvas.addEventListener("wheel", function (e) {
    if (state.mode !== "playing") return;
    state.activeInput = "mouse";
    state.cameraMode = (state.cameraMode + (e.deltaY > 0 ? 1 : 2)) % 3;
  }, { passive: true });

  // Touch swipe & drag controls
  input.touchLastTime = 0;
  canvas.addEventListener("touchstart", function (e) {
    if (!e.touches || !e.touches.length) return;
    state.activeInput = "touch";
    input.touchLastTime = performance.now();
    input.touchDragActive = true;
    input.touchStartX = e.touches[0].clientX;
    input.touchStartY = e.touches[0].clientY;
    input.touchLastX = input.touchStartX;
    input.touchMoved = false;
  }, { passive: true });

  canvas.addEventListener("touchmove", function (e) {
    if (!input.touchDragActive || !e.touches || !e.touches.length || state.mode !== "playing") return;
    input.touchLastTime = performance.now();
    const cx = e.touches[0].clientX;
    const cy = e.touches[0].clientY;
    const dx = cx - input.touchLastX;
    input.touchLastX = cx;

    const totalDx = cx - input.touchStartX;
    const totalDy = cy - input.touchStartY;
    if (Math.abs(totalDx) > 10 || Math.abs(totalDy) > 10) {
      input.touchMoved = true;
    }

    // Smooth horizontal steering proportional to finger slide
    state.playerX += (dx / Math.max(200, window.innerWidth)) * (ROAD_WIDTH * 1.65);
    state.playerX = Math.max(-HALF_ROAD, Math.min(HALF_ROAD, state.playerX));

    // Swipe Up for Jump
    if (totalDy < -48) {
      triggerJump();
      input.touchStartY = cy;
    } else if (totalDy > 54 && !state.isGrounded) {
      state.playerVelY = Math.min(state.playerVelY, -14);
      input.touchStartY = cy;
    }
  }, { passive: true });

  canvas.addEventListener("touchend", function () {
    input.touchLastTime = performance.now();
    if (!input.touchMoved && state.mode === "playing") {
      // Quick tap on 3D road jumps!
      triggerJump();
    }
    input.touchDragActive = false;
  }, { passive: true });

  window.addEventListener("resize", function () {
    camera.aspect = window.innerWidth / window.innerHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(window.innerWidth, window.innerHeight);
  });

  // Main Animation & Physics Loop
  let lastTime = performance.now();
  let particleAccum = 0;

  function animate(now) {
    requestAnimationFrame(animate);
    const dt = Math.min(0.05, (now - lastTime) / 1000);
    lastTime = now;

    if (state.mode === "paused") {
      renderer.render(scene, camera);
      return;
    }

    const isPlaying = state.mode === "playing";
    const isMenu = state.mode === "menu";
    const isTurbo = state.turboTimer > 0;

    if (isPlaying) {
      state.baseSpeed = Math.min(48, 24 + state.distance * 0.012);
      state.speed = isTurbo ? state.baseSpeed * 1.62 : state.baseSpeed;
      state.distance += state.speed * dt;
      state.score += state.speed * dt * (isTurbo ? 2.2 : 1.0);

      if (state.turboTimer > 0) state.turboTimer = Math.max(0, state.turboTimer - dt);
      if (state.magnetTimer > 0) state.magnetTimer = Math.max(0, state.magnetTimer - dt);
      if (state.invulnTimer > 0) state.invulnTimer = Math.max(0, state.invulnTimer - dt);
      if (state.shakeTimer > 0) state.shakeTimer = Math.max(0, state.shakeTimer - dt);
      if (state.comboTimer > 0) {
        state.comboTimer -= dt;
        if (state.comboTimer <= 0) state.combo = 0;
      }

      // Passive turbo recharge
      if (!isTurbo) {
        state.turboCharge = Math.min(100, state.turboCharge + dt * 4.5);
      }

      // Horizontal Steering
      let steerDir = 0;
      if (input.left) steerDir -= 1;
      if (input.right) steerDir += 1;

      if (steerDir !== 0) {
        state.playerX += steerDir * 10.5 * dt;
      } else if (input.mouseTargetX !== null && now < input.mouseActiveUntil) {
        const diff = input.mouseTargetX - state.playerX;
        state.playerX += diff * Math.min(1, dt * 10.0);
        steerDir = Math.max(-1, Math.min(1, diff * 0.6));
      }

      state.playerX = Math.max(-HALF_ROAD, Math.min(HALF_ROAD, state.playerX));

      // Jump & Gravity
      if (!state.isGrounded) {
        state.playerVelY -= 32.0 * dt;
        state.playerY += state.playerVelY * dt;
        if (state.playerY <= 0) {
          state.playerY = 0;
          state.playerVelY = 0;
          state.isGrounded = true;
        }
      }

      // Bajaj visual animation
      bajaj.group.position.set(state.playerX, state.playerY, 0);
      bajaj.group.rotation.z = THREE.MathUtils.lerp(bajaj.group.rotation.z, -steerDir * 0.14, dt * 12);
      bajaj.group.rotation.y = THREE.MathUtils.lerp(bajaj.group.rotation.y, -steerDir * 0.1, dt * 12);
      bajaj.frontWheel.rotation.y = THREE.MathUtils.lerp(bajaj.frontWheel.rotation.y, -steerDir * 0.35, dt * 12);

      // Invulnerability blink
      bajaj.group.visible = state.invulnTimer <= 0 || Math.floor(now / 70) % 2 === 0;
      bajaj.shieldSphere.visible = state.hasShield;
      bajaj.shieldSphere.rotation.y += dt * 2.2;
      bajaj.turboGlow.visible = isTurbo;
      if (isTurbo) {
        bajaj.turboGlow.scale.setScalar(0.85 + Math.sin(now * 0.04) * 0.2);
      }

      // Spawn obstacles & coins
      spawnObstacleTimer -= dt;
      if (spawnObstacleTimer <= 0) {
        spawnObstacle();
        spawnObstacleTimer = Math.max(0.45, 1.35 - (state.speed - 24) * 0.025) + Math.random() * 0.4;
      }

      spawnCoinTimer -= dt;
      if (spawnCoinTimer <= 0) {
        spawnCollectible();
        spawnCoinTimer = 0.42 + Math.random() * 0.35;
      }

      // Update obstacles & collision
      for (let i = obstacles.length - 1; i >= 0; i--) {
        const obs = obstacles[i];
        obs.z += state.speed * dt;
        obs.mesh.position.z = obs.z;

        // Check collision
        if (Math.abs(obs.z) < 1.35 && Math.abs(state.playerX - obs.x) < (obs.halfW + 0.56)) {
          const clearedByJump = obs.jumpable && state.playerY > obs.height * 0.85;
          if (!clearedByJump && state.invulnTimer <= 0) {
            if (isTurbo) {
              // Smash through during turbo!
              scene.remove(obs.mesh);
              obstacles.splice(i, 1);
              state.score += 50;
              state.shakeTimer = 0.18;
              audio.playPowerup();
              continue;
            } else if (state.hasShield) {
              state.hasShield = false;
              state.invulnTimer = 1.6;
              state.shakeTimer = 0.25;
              scene.remove(obs.mesh);
              obstacles.splice(i, 1);
              audio.playCrash();
              syncHud(true);
              continue;
            } else {
              // Game Over!
              state.mode = "gameover";
              state.shakeTimer = 0.4;
              audio.playCrash();
              syncHud(true);
              notifyBridge("onGameOver", JSON.stringify({
                score: Math.floor(state.score),
                coins: state.coins,
                distance: Math.floor(state.distance)
              }));
              break;
            }
          }
        }

        if (obs.z > 14) {
          scene.remove(obs.mesh);
          obstacles.splice(i, 1);
        }
      }

      // Update collectibles
      for (let j = collectibles.length - 1; j >= 0; j--) {
        const c = collectibles[j];
        c.z += state.speed * dt;
        c.mesh.rotation.z += dt * 3.5;
        c.mesh.rotation.y += dt * 2.4;

        // Magnet Rezeki attraction
        if (state.magnetTimer > 0 && c.kind === "coin" && c.z > -28 && c.z < 4) {
          c.x = THREE.MathUtils.lerp(c.x, state.playerX, dt * 8.5);
          c.mesh.position.x = c.x;
        }
        c.mesh.position.z = c.z;

        if (Math.abs(c.z) < 1.45 && Math.abs(state.playerX - c.x) < 1.25 && state.playerY < 1.85) {
          if (c.kind === "coin") {
            state.coins += 1;
            state.combo += 1;
            state.comboTimer = 2.6;
            state.score += 15 + Math.min(35, state.combo * 2);
            state.turboCharge = Math.min(100, state.turboCharge + 10);
            audio.playCoin(state.combo);
          } else if (c.kind === "shield") {
            state.hasShield = true;
            audio.playPowerup();
          } else if (c.kind === "magnet") {
            state.magnetTimer = 7.5;
            audio.playPowerup();
          } else if (c.kind === "nasi") {
            state.score += 75;
            state.turboCharge = Math.min(100, state.turboCharge + 35);
            audio.playPowerup();
          }
          scene.remove(c.mesh);
          collectibles.splice(j, 1);
          continue;
        }

        if (c.z > 14) {
          scene.remove(c.mesh);
          collectibles.splice(j, 1);
        }
      }

      audio.tickGamelanPulse(now / 1000, true, isTurbo);
      syncHud(false);
    } else if (isMenu) {
      // Live 3D showcase cruise in Main Menu
      state.speed = 16;
      const cruiseX = Math.sin(now * 0.0008) * 2.2;
      bajaj.group.position.set(cruiseX, 0, 0);
      bajaj.group.rotation.z = -Math.cos(now * 0.0008) * 0.08;
      bajaj.group.visible = true;
      bajaj.shieldSphere.visible = false;
      bajaj.turboGlow.visible = false;
    }

    // Scroll road, terrain & roadside props
    if (isPlaying || isMenu) {
      const worldSpeed = isPlaying ? state.speed : 16;
      if (roadTexture) roadTexture.offset.y -= worldSpeed * dt * 0.054;
      if (groundTexture) groundTexture.offset.y -= worldSpeed * dt * 0.107;

      // Spin Bajaj wheels
      const wheelSpin = worldSpeed * dt * 2.6;
      bajaj.frontWheel.rotation.x -= wheelSpin;
      bajaj.rearL.rotation.x -= wheelSpin;
      bajaj.rearR.rotation.x -= wheelSpin;

      for (let k = 0; k < propsPool.length; k++) {
        const p = propsPool[k];
        p.position.z += worldSpeed * dt;
        if (p.position.z > 16) {
          p.position.z -= 176;
        }
      }

      // Exhaust puffs
      particleAccum += dt;
      if (particleAccum > (isTurbo ? 0.035 : 0.085)) {
        particleAccum = 0;
        emitExhaustParticle(isTurbo);
      }
    }

    // Update particles
    for (let m = particles.length - 1; m >= 0; m--) {
      const pt = particles[m];
      pt.life -= dt;
      pt.mesh.position.x += pt.vx * dt;
      pt.mesh.position.y += pt.vy * dt;
      pt.mesh.position.z += pt.vz * dt;
      pt.mesh.scale.addScalar(dt * 1.4);
      pt.mesh.material.opacity = Math.max(0, pt.life * 1.4);
      if (pt.life <= 0) {
        scene.remove(pt.mesh);
        particles.splice(m, 1);
      }
    }

    // Smooth camera follow + shake
    let targetCamY = 4.2;
    let targetCamZ = 8.4;
    if (state.cameraMode === 1) {
      targetCamY = 6.8;
      targetCamZ = 10.8;
    } else if (state.cameraMode === 2) {
      targetCamY = 2.8;
      targetCamZ = 6.2;
    }
    const shakeX = state.shakeTimer > 0 ? (Math.random() - 0.5) * 0.35 : 0;
    const shakeY = state.shakeTimer > 0 ? (Math.random() - 0.5) * 0.25 : 0;
    camera.position.x = THREE.MathUtils.lerp(camera.position.x, bajaj.group.position.x * 0.62, dt * 6) + shakeX;
    camera.position.y = THREE.MathUtils.lerp(camera.position.y, targetCamY + state.playerY * 0.4, dt * 6) + shakeY;
    camera.position.z = THREE.MathUtils.lerp(camera.position.z, targetCamZ + (isTurbo ? 1.1 : 0), dt * 5);
    camera.lookAt(bajaj.group.position.x * 0.45, 1.35 + state.playerY * 0.35, -10);

    renderer.render(scene, camera);
  }

  // Exposed Bridge API for Jetpack Compose / Kotlin
  window.GameAPI = {
    startGame: function (stageIdx) {
      resetRun(typeof stageIdx === "number" ? stageIdx : state.stage);
    },
    setStagePreview: function (stageIdx) {
      if (state.stage !== stageIdx) {
        state.stage = stageIdx;
        setupEnvironment(stageIdx);
      }
    },
    pauseGame: function () {
      if (state.mode === "playing") {
        state.mode = "paused";
        syncHud(true);
      }
    },
    resumeGame: function () {
      if (state.mode === "paused") {
        state.mode = "playing";
        lastTime = performance.now();
        audio.ensureContext();
        syncHud(true);
      }
    },
    returnToMenu: function () {
      clearEntities();
      state.mode = "menu";
      state.playerX = 0;
      state.playerY = 0;
      syncHud(true);
    },
    setSoundEnabled: function (enabled) {
      audio.setEnabled(enabled);
    },
    setMouseSteerEnabled: function (enabled) {
      state.mouseSteerEnabled = !!enabled;
      if (!enabled) input.mouseTargetX = null;
    },
    cycleCamera: function () {
      state.cameraMode = (state.cameraMode + 1) % 3;
      syncHud(true);
    },
    setTouchInput: function (action, pressed) {
      state.activeInput = "touch";
      if (action === "left") input.left = !!pressed;
      if (action === "right") input.right = !!pressed;
      if (action === "jump" && pressed) triggerJump();
      if (action === "turbo" && pressed) triggerTurbo();
    },
    handleNativeKey: function (code, isDown) {
      handleKeyCode(code, !!isDown);
    }
  };

  // Initialize Jakarta stage & notify Android
  setupEnvironment(0);
  notifyBridge("onEngineReady", "");
  requestAnimationFrame(animate);
})();
