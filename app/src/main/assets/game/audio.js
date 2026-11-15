(function (global) {
  "use strict";

  class NusantaraAudio {
    constructor() {
      this.ctx = null;
      this.enabled = true;
      this.gamelanStep = 0;
      this.lastGamelanTime = 0;
      // Slendro-inspired pentatonic frequencies (C5, D5, E5, G5, A5, C6)
      this.slendro = [523.25, 587.33, 659.25, 783.99, 880.0, 1046.5];
      this.melodyPattern = [0, 2, 4, 3, 2, 4, 5, 3, 1, 2, 0, 3];
    }

    ensureContext() {
      if (!this.enabled) return null;
      if (!this.ctx) {
        const AudioCtx = window.AudioContext || window.webkitAudioContext;
        if (AudioCtx) {
          this.ctx = new AudioCtx();
        }
      }
      if (this.ctx && this.ctx.state === "suspended") {
        this.ctx.resume().catch(function () {});
      }
      return this.ctx;
    }

    setEnabled(val) {
      this.enabled = !!val;
      if (!this.enabled && this.ctx && this.ctx.state === "running") {
        this.ctx.suspend().catch(function () {});
      } else if (this.enabled && this.ctx && this.ctx.state === "suspended") {
        this.ctx.resume().catch(function () {});
      }
    }

    playCoin(combo) {
      const ctx = this.ensureContext();
      if (!ctx) return;
      const now = ctx.currentTime;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      const baseFreq = this.slendro[(combo || 0) % this.slendro.length];
      osc.type = "triangle";
      osc.frequency.setValueAtTime(baseFreq, now);
      osc.frequency.exponentialRampToValueAtTime(baseFreq * 1.5, now + 0.09);

      gain.gain.setValueAtTime(0.14, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.16);

      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now);
      osc.stop(now + 0.17);
    }

    playJump() {
      const ctx = this.ensureContext();
      if (!ctx) return;
      const now = ctx.currentTime;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sine";
      osc.frequency.setValueAtTime(210, now);
      osc.frequency.exponentialRampToValueAtTime(490, now + 0.14);

      gain.gain.setValueAtTime(0.16, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.18);

      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now);
      osc.stop(now + 0.19);
    }

    playKlaksonTurbo() {
      const ctx = this.ensureContext();
      if (!ctx) return;
      const now = ctx.currentTime;
      // Classic double-beep Bajaj klakson ("Tin-tin!")
      [0, 0.13].forEach(function (offset) {
        const osc1 = ctx.createOscillator();
        const osc2 = ctx.createOscillator();
        const gain = ctx.createGain();
        osc1.type = "sawtooth";
        osc2.type = "triangle";
        osc1.frequency.setValueAtTime(440, now + offset);
        osc2.frequency.setValueAtTime(554.37, now + offset);

        gain.gain.setValueAtTime(0.09, now + offset);
        gain.gain.exponentialRampToValueAtTime(0.001, now + offset + 0.11);

        osc1.connect(gain);
        osc2.connect(gain);
        gain.connect(ctx.destination);
        osc1.start(now + offset);
        osc2.start(now + offset);
        osc1.stop(now + offset + 0.12);
        osc2.stop(now + offset + 0.12);
      });
    }

    playPowerup() {
      const ctx = this.ensureContext();
      if (!ctx) return;
      const now = ctx.currentTime;
      const notes = [523.25, 659.25, 783.99, 1046.5];
      notes.forEach(function (freq, idx) {
        const osc = ctx.createOscillator();
        const gain = ctx.createGain();
        osc.type = "sine";
        osc.frequency.setValueAtTime(freq, now + idx * 0.055);
        gain.gain.setValueAtTime(0.11, now + idx * 0.055);
        gain.gain.exponentialRampToValueAtTime(0.001, now + idx * 0.055 + 0.16);
        osc.connect(gain);
        gain.connect(ctx.destination);
        osc.start(now + idx * 0.055);
        osc.stop(now + idx * 0.055 + 0.17);
      });
    }

    playCrash() {
      const ctx = this.ensureContext();
      if (!ctx) return;
      const now = ctx.currentTime;
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sawtooth";
      osc.frequency.setValueAtTime(140, now);
      osc.frequency.exponentialRampToValueAtTime(42, now + 0.35);

      gain.gain.setValueAtTime(0.24, now);
      gain.gain.exponentialRampToValueAtTime(0.001, now + 0.36);

      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now);
      osc.stop(now + 0.38);
    }

    tickGamelanPulse(timeSec, isPlaying, isTurbo) {
      if (!this.enabled || !isPlaying || !this.ctx || this.ctx.state !== "running") return;
      const interval = isTurbo ? 0.19 : 0.28;
      if (timeSec - this.lastGamelanTime < interval) return;
      this.lastGamelanTime = timeSec;

      const ctx = this.ctx;
      const now = ctx.currentTime;
      const noteIdx = this.melodyPattern[this.gamelanStep % this.melodyPattern.length];
      this.gamelanStep++;

      const osc = ctx.createOscillator();
      const gain = ctx.createGain();
      osc.type = "sine";
      const freq = this.slendro[noteIdx] * 0.5;
      osc.frequency.setValueAtTime(freq, now);

      gain.gain.setValueAtTime(0.038, now);
      gain.gain.exponentialRampToValueAtTime(0.0008, now + 0.24);

      osc.connect(gain);
      gain.connect(ctx.destination);
      osc.start(now);
      osc.stop(now + 0.25);
    }
  }

  global.NusantaraAudio = NusantaraAudio;
})(window);
