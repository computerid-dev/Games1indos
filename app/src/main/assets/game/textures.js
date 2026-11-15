(function (global) {
  "use strict";

  function makeAsphaltTexture(stageIndex) {
    const canvas = document.createElement("canvas");
    canvas.width = 512;
    canvas.height = 512;
    const ctx = canvas.getContext("2d");

    // Base road tone per stage
    const baseColors = ["#272930", "#2f2c33", "#2b3036"];
    ctx.fillStyle = baseColors[stageIndex] || "#272930";
    ctx.fillRect(0, 0, 512, 512);

    // Subtle aggregate grain
    for (let i = 0; i < 3200; i++) {
      const x = Math.random() * 512;
      const y = Math.random() * 512;
      const v = 30 + Math.floor(Math.random() * 32);
      ctx.fillStyle = "rgba(" + v + "," + v + "," + (v + 4) + ",0.35)";
      ctx.fillRect(x, y, 2, 2);
    }

    // Side red-white Indonesian curb markers (kurb jalan)
    const curbWidth = 22;
    for (let y = 0; y < 512; y += 64) {
      ctx.fillStyle = (y / 64) % 2 === 0 ? "#d32f2f" : "#f5f5f5";
      ctx.fillRect(0, y, curbWidth, 64);
      ctx.fillRect(512 - curbWidth, y, curbWidth, 64);
    }

    // Outer solid white edge lines
    ctx.fillStyle = "rgba(255,255,255,0.85)";
    ctx.fillRect(32, 0, 8, 512);
    ctx.fillRect(512 - 40, 0, 8, 512);

    // Three-lane dashed yellow Indonesian road dividers
    ctx.fillStyle = "#ffc107";
    const lane1X = Math.floor(512 * 0.36);
    const lane2X = Math.floor(512 * 0.64);
    for (let y = 16; y < 512; y += 96) {
      ctx.fillRect(lane1X - 4, y, 8, 54);
      ctx.fillRect(lane2X - 4, y, 8, 54);
    }

    const tex = new THREE.CanvasTexture(canvas);
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(1, 14);
    return tex;
  }

  function makeBatikTexture(primary, accent) {
    const canvas = document.createElement("canvas");
    canvas.width = 256;
    canvas.height = 256;
    const ctx = canvas.getContext("2d");

    ctx.fillStyle = primary;
    ctx.fillRect(0, 0, 256, 256);

    // Classic Parang / Kawung inspired geometric motif
    ctx.strokeStyle = accent;
    ctx.lineWidth = 4;
    for (let y = 0; y < 256; y += 32) {
      for (let x = 0; x < 256; x += 32) {
        ctx.beginPath();
        ctx.arc(x + 16, y + 16, 12, 0, Math.PI * 2);
        ctx.stroke();

        ctx.fillStyle = accent;
        ctx.beginPath();
        ctx.arc(x + 16, y + 16, 3.5, 0, Math.PI * 2);
        ctx.fill();
      }
    }

    const tex = new THREE.CanvasTexture(canvas);
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    return tex;
  }

  function makeGroundTexture(stageIndex) {
    const canvas = document.createElement("canvas");
    canvas.width = 256;
    canvas.height = 256;
    const ctx = canvas.getContext("2d");

    const palettes = [
      ["#2e4c2a", "#3a5e33"], // Jakarta urban grass
      ["#3b3528", "#4d4534"], // Yogyakarta volcanic stone & soil
      ["#c8a86b", "#45884a"]  // Bali coastal sand & tropical turf
    ];
    const p = palettes[stageIndex] || palettes[0];
    ctx.fillStyle = p[0];
    ctx.fillRect(0, 0, 256, 256);

    for (let i = 0; i < 650; i++) {
      ctx.fillStyle = p[1];
      ctx.fillRect(Math.random() * 256, Math.random() * 256, 4, 4);
    }

    const tex = new THREE.CanvasTexture(canvas);
    tex.wrapS = THREE.RepeatWrapping;
    tex.wrapT = THREE.RepeatWrapping;
    tex.repeat.set(8, 28);
    return tex;
  }

  global.NusantaraTextures = {
    makeAsphaltTexture: makeAsphaltTexture,
    makeBatikTexture: makeBatikTexture,
    makeGroundTexture: makeGroundTexture
  };
})(window);
