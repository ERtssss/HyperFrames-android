package com.saalpa.data

import com.saalpa.model.AspectRatioType
import com.saalpa.model.ParamType
import com.saalpa.model.TemplateParam
import com.saalpa.model.VideoTemplate

object TemplateRepository {

    val templates: List<VideoTemplate> = listOf(
        // 1. Kinetic Typography
        VideoTemplate(
            id = "kinetic_typography",
            name = "Kinetic Typography",
            category = "Social & Reels",
            description = "Bold, energetic motion text reveal with glowing accents and audio waveform bars.",
            defaultDurationSec = 3.5f,
            defaultFps = 30,
            defaultAspectRatio = AspectRatioType.PORTRAIT_9_16,
            params = listOf(
                TemplateParam("HEADER", "Top Tag", "⚡ HYPER FRAMES"),
                TemplateParam("MAIN_TITLE", "Main Heading", "CREATE VIDEOS"),
                TemplateParam("HIGHLIGHT", "Highlighted Word", "FROM HTML"),
                TemplateParam("SUBTITLE", "Bottom Subtitle", "Hardware Rendered on Android"),
                TemplateParam("ACCENT_COLOR", "Accent Color", "#00F0FF", type = ParamType.COLOR_HEX)
            ),
            htmlBody = """
                <div class="kinetic-container">
                    <div class="bg-glow"></div>
                    <div class="content-box">
                        <div class="badge-tag">{{HEADER}}</div>
                        <h1 class="main-line line-1">{{MAIN_TITLE}}</h1>
                        <h1 class="main-line line-2 highlight">{{HIGHLIGHT}}</h1>
                        <p class="subtitle">{{SUBTITLE}}</p>
                    </div>
                    <div class="eq-bars">
                        <div class="bar bar-1"></div>
                        <div class="bar bar-2"></div>
                        <div class="bar bar-3"></div>
                        <div class="bar bar-4"></div>
                        <div class="bar bar-5"></div>
                        <div class="bar bar-6"></div>
                        <div class="bar bar-7"></div>
                    </div>
                </div>
            """.trimIndent(),
            cssStyle = """
                .kinetic-container {
                    width: 100%;
                    height: 100%;
                    background: radial-gradient(circle at 50% 40%, #171b30 0%, #080a11 100%);
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    position: relative;
                    padding: 40px 24px;
                    text-align: center;
                    box-sizing: border-box;
                }
                .bg-glow {
                    position: absolute;
                    width: 320px;
                    height: 320px;
                    background: radial-gradient(circle, {{ACCENT_COLOR}}44 0%, transparent 70%);
                    filter: blur(40px);
                    transform: scale(calc(0.8 + var(--progress) * 0.5));
                    opacity: calc(0.5 + var(--progress) * 0.5);
                }
                .content-box {
                    position: relative;
                    z-index: 2;
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                }
                .badge-tag {
                    display: inline-block;
                    padding: 8px 18px;
                    background: rgba(255,255,255,0.08);
                    border: 1px solid {{ACCENT_COLOR}};
                    color: {{ACCENT_COLOR}};
                    border-radius: 999px;
                    font-size: 14px;
                    font-weight: 700;
                    letter-spacing: 3px;
                    margin-bottom: 24px;
                    transform: translateY(calc((1 - min(var(--progress) * 3, 1)) * -30px));
                    opacity: calc(min(var(--progress) * 3, 1));
                    box-shadow: 0 0 20px {{ACCENT_COLOR}}55;
                }
                .main-line {
                    font-size: 42px;
                    font-weight: 900;
                    line-height: 1.15;
                    color: #ffffff;
                    text-transform: uppercase;
                    letter-spacing: calc((1 - min(var(--progress) * 2, 1)) * 12px);
                    transform: scale(calc(0.6 + min(var(--progress) * 1.5, 1) * 0.4));
                    opacity: calc(min(var(--progress) * 2, 1));
                }
                .line-2.highlight {
                    color: {{ACCENT_COLOR}};
                    text-shadow: 0 0 30px {{ACCENT_COLOR}}aa, 0 0 60px {{ACCENT_COLOR}}44;
                    margin-top: 4px;
                    transform: scale(calc(0.7 + min(max(var(--progress) - 0.2, 0) * 2, 1) * 0.3));
                    opacity: calc(min(max(var(--progress) - 0.2, 0) * 3, 1));
                }
                .subtitle {
                    font-size: 16px;
                    color: #94a3b8;
                    margin-top: 28px;
                    font-weight: 500;
                    letter-spacing: 1px;
                    transform: translateY(calc((1 - min(max(var(--progress) - 0.4, 0) * 2.5, 1)) * 25px));
                    opacity: calc(min(max(var(--progress) - 0.4, 0) * 2.5, 1));
                }
                .eq-bars {
                    position: absolute;
                    bottom: 40px;
                    display: flex;
                    gap: 6px;
                    align-items: flex-end;
                    height: 40px;
                }
                .bar {
                    width: 5px;
                    background: {{ACCENT_COLOR}};
                    border-radius: 4px;
                    opacity: 0.8;
                }
                .bar-1 { height: calc(10px + sin(var(--progress) * 18 + 1) * 20px); }
                .bar-2 { height: calc(12px + sin(var(--progress) * 22 + 2) * 25px); }
                .bar-3 { height: calc(15px + sin(var(--progress) * 26 + 3) * 28px); }
                .bar-4 { height: calc(18px + sin(var(--progress) * 30 + 4) * 30px); }
                .bar-5 { height: calc(15px + sin(var(--progress) * 24 + 5) * 28px); }
                .bar-6 { height: calc(12px + sin(var(--progress) * 20 + 6) * 25px); }
                .bar-7 { height: calc(10px + sin(var(--progress) * 16 + 7) * 20px); }
            """.trimIndent(),
            jsScript = ""
        ),

        // 2. Cyberpunk Neon HUD
        VideoTemplate(
            id = "cyberpunk_hud",
            name = "Cyberpunk Sci-Fi HUD",
            category = "Tech & Gaming",
            description = "Futuristic neon interface with scanning lasers, glowing reticle, and tech telemetry.",
            defaultDurationSec = 4.0f,
            defaultFps = 30,
            defaultAspectRatio = AspectRatioType.PORTRAIT_9_16,
            params = listOf(
                TemplateParam("TARGET_SYS", "System Name", "NEO_CORE v4.9"),
                TemplateParam("STATUS_TXT", "Status Text", "SYSTEM ONLINE"),
                TemplateParam("CODE_VAL", "Hex Data", "0x7F // INITIALIZED"),
                TemplateParam("ACCENT_COLOR", "Neon Color", "#FF2A85", type = ParamType.COLOR_HEX)
            ),
            htmlBody = """
                <div class="hud-frame">
                    <div class="grid-layer"></div>
                    <div class="laser-scanner"></div>
                    
                    <div class="hud-header">
                        <span class="sys-label">{{TARGET_SYS}}</span>
                        <span class="rec-dot">● REC</span>
                    </div>

                    <div class="reticle-box">
                        <div class="circle-outer"></div>
                        <div class="circle-inner"></div>
                        <div class="reticle-cross"></div>
                        <div class="reticle-core">{{STATUS_TXT}}</div>
                    </div>

                    <div class="hud-footer">
                        <div class="telemetry">
                            <span>FPS: 60.0</span>
                            <span>NODE: 192.168.0.1</span>
                            <span>DATA: {{CODE_VAL}}</span>
                        </div>
                        <div class="progress-track">
                            <div class="progress-fill"></div>
                        </div>
                    </div>
                </div>
            """.trimIndent(),
            cssStyle = """
                .hud-frame {
                    width: 100%;
                    height: 100%;
                    background: #05060d;
                    color: #fff;
                    display: flex;
                    flex-direction: column;
                    justify-content: space-between;
                    padding: 36px 24px;
                    box-sizing: border-box;
                    position: relative;
                    overflow: hidden;
                    font-family: monospace;
                }
                .grid-layer {
                    position: absolute;
                    inset: 0;
                    background-image: linear-gradient(rgba(255, 42, 133, 0.08) 1px, transparent 1px),
                                      linear-gradient(90deg, rgba(255, 42, 133, 0.08) 1px, transparent 1px);
                    background-size: 24px 24px;
                }
                .laser-scanner {
                    position: absolute;
                    left: 0;
                    width: 100%;
                    height: 3px;
                    background: {{ACCENT_COLOR}};
                    box-shadow: 0 0 15px {{ACCENT_COLOR}}, 0 0 30px {{ACCENT_COLOR}};
                    top: calc(var(--progress) * 100%);
                    opacity: 0.8;
                }
                .hud-header {
                    display: flex;
                    justify-content: space-between;
                    align-items: center;
                    border-bottom: 1px solid rgba(255,255,255,0.15);
                    padding-bottom: 12px;
                    font-size: 13px;
                    letter-spacing: 2px;
                    z-index: 2;
                }
                .sys-label {
                    color: {{ACCENT_COLOR}};
                    font-weight: bold;
                }
                .rec-dot {
                    color: #ff3344;
                    font-weight: bold;
                }
                .reticle-box {
                    position: relative;
                    width: 220px;
                    height: 220px;
                    margin: 0 auto;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 2;
                }
                .circle-outer {
                    position: absolute;
                    inset: 0;
                    border: 2px dashed {{ACCENT_COLOR}};
                    border-radius: 50%;
                    box-shadow: 0 0 25px {{ACCENT_COLOR}}44;
                    transform: rotate(calc(var(--progress) * 360deg));
                }
                .circle-inner {
                    position: absolute;
                    inset: 30px;
                    border: 2px solid rgba(255,255,255,0.4);
                    border-top-color: {{ACCENT_COLOR}};
                    border-radius: 50%;
                    transform: rotate(calc(var(--progress) * -540deg));
                }
                .reticle-core {
                    font-size: 16px;
                    font-weight: 900;
                    color: #fff;
                    letter-spacing: 2px;
                    text-shadow: 0 0 10px {{ACCENT_COLOR}};
                    transform: scale(calc(0.8 + sin(var(--progress) * 12) * 0.15));
                }
                .hud-footer {
                    z-index: 2;
                    border-top: 1px solid rgba(255,255,255,0.15);
                    padding-top: 14px;
                }
                .telemetry {
                    display: flex;
                    justify-content: space-between;
                    font-size: 11px;
                    color: #94a3b8;
                    margin-bottom: 10px;
                }
                .progress-track {
                    width: 100%;
                    height: 6px;
                    background: rgba(255,255,255,0.1);
                    border-radius: 3px;
                    overflow: hidden;
                }
                .progress-fill {
                    height: 100%;
                    width: calc(var(--progress) * 100%);
                    background: {{ACCENT_COLOR}};
                    box-shadow: 0 0 10px {{ACCENT_COLOR}};
                }
            """.trimIndent(),
            jsScript = ""
        ),

        // 3. Minimalist Aesthetic Story
        VideoTemplate(
            id = "aesthetic_story",
            name = "Aesthetic Aura Story",
            category = "Minimal & Lifestyle",
            description = "Soft glowing aura gradients, serif typography, and elegant editorial float motion.",
            defaultDurationSec = 3.0f,
            defaultFps = 30,
            defaultAspectRatio = AspectRatioType.PORTRAIT_9_16,
            params = listOf(
                TemplateParam("TAG", "Top Category", "EDITION NO. 04"),
                TemplateParam("TITLE", "Main Title", "Pure Serenity"),
                TemplateParam("QUOTE", "Quote / Description", "Beauty begins the moment you decide to be yourself."),
                TemplateParam("PRIMARY_AURA", "Aura Color 1", "#F59E0B", type = ParamType.COLOR_HEX),
                TemplateParam("SECONDARY_AURA", "Aura Color 2", "#EC4899", type = ParamType.COLOR_HEX)
            ),
            htmlBody = """
                <div class="aesthetic-wrapper">
                    <div class="aura-blob aura-1"></div>
                    <div class="aura-blob aura-2"></div>
                    <div class="glass-card">
                        <span class="card-tag">{{TAG}}</span>
                        <h2 class="card-title">{{TITLE}}</h2>
                        <div class="divider"></div>
                        <p class="card-quote">“{{QUOTE}}”</p>
                    </div>
                </div>
            """.trimIndent(),
            cssStyle = """
                .aesthetic-wrapper {
                    width: 100%;
                    height: 100%;
                    background: #0f111a;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    position: relative;
                    overflow: hidden;
                    padding: 30px;
                    box-sizing: border-box;
                }
                .aura-blob {
                    position: absolute;
                    border-radius: 50%;
                    filter: blur(60px);
                    opacity: 0.6;
                }
                .aura-1 {
                    width: 280px;
                    height: 280px;
                    background: {{PRIMARY_AURA}};
                    top: calc(20% + sin(var(--progress) * 6) * 40px);
                    left: calc(20% + cos(var(--progress) * 6) * 30px);
                    transform: scale(calc(0.9 + var(--progress) * 0.3));
                }
                .aura-2 {
                    width: 240px;
                    height: 240px;
                    background: {{SECONDARY_AURA}};
                    bottom: calc(20% - sin(var(--progress) * 6) * 40px);
                    right: calc(15% - cos(var(--progress) * 6) * 30px);
                    transform: scale(calc(1.1 - var(--progress) * 0.2));
                }
                .glass-card {
                    position: relative;
                    z-index: 3;
                    background: rgba(255, 255, 255, 0.06);
                    backdrop-filter: blur(24px);
                    -webkit-backdrop-filter: blur(24px);
                    border: 1px solid rgba(255, 255, 255, 0.18);
                    border-radius: 28px;
                    padding: 42px 30px;
                    text-align: center;
                    box-shadow: 0 30px 60px rgba(0,0,0,0.4);
                    transform: translateY(calc((1 - min(var(--progress) * 2, 1)) * 40px));
                    opacity: calc(min(var(--progress) * 2.5, 1));
                }
                .card-tag {
                    font-size: 12px;
                    letter-spacing: 4px;
                    text-transform: uppercase;
                    color: rgba(255, 255, 255, 0.7);
                    font-weight: 600;
                }
                .card-title {
                    font-family: Georgia, serif;
                    font-size: 38px;
                    font-weight: 400;
                    color: #ffffff;
                    margin: 18px 0;
                    letter-spacing: 0.5px;
                }
                .divider {
                    width: 40px;
                    height: 2px;
                    background: {{PRIMARY_AURA}};
                    margin: 0 auto 20px auto;
                    transform: scaleX(calc(min(var(--progress) * 3, 1)));
                }
                .card-quote {
                    font-size: 15px;
                    line-height: 1.6;
                    color: rgba(255, 255, 255, 0.85);
                    font-style: italic;
                }
            """.trimIndent(),
            jsScript = ""
        ),

        // 4. Code Snippet IDE Typer
        VideoTemplate(
            id = "code_typer",
            name = "Code Typer IDE",
            category = "Dev & Tech",
            description = "Animated IDE editor with syntax highlighting, typing effect, and macOS window bar.",
            defaultDurationSec = 3.5f,
            defaultFps = 30,
            defaultAspectRatio = AspectRatioType.PORTRAIT_9_16,
            params = listOf(
                TemplateParam("FILE_NAME", "File Name", "hyperframe.ts"),
                TemplateParam("FUNCTION_NAME", "Function", "renderVideo"),
                TemplateParam("PARAM_NAME", "Engine Config", "format: 'mp4', fps: 60"),
                TemplateParam("THEME_COLOR", "Accent Color", "#10B981", type = ParamType.COLOR_HEX)
            ),
            htmlBody = """
                <div class="ide-container">
                    <div class="window-frame">
                        <div class="titlebar">
                            <div class="window-dots">
                                <span class="dot red"></span>
                                <span class="dot yellow"></span>
                                <span class="dot green"></span>
                            </div>
                            <span class="file-tab">{{FILE_NAME}}</span>
                        </div>
                        <div class="code-body">
                            <div class="line"><span class="ln">1</span><span class="kw">import</span> { HyperFrames } <span class="kw">from</span> <span class="str">'@hyper/core'</span>;</div>
                            <div class="line"><span class="ln">2</span></div>
                            <div class="line"><span class="ln">3</span><span class="kw">export async function</span> <span class="fn">{{FUNCTION_NAME}}</span>() {</div>
                            <div class="line"><span class="ln">4</span>  <span class="kw">const</span> engine = <span class="kw">new</span> <span class="cls">HyperFrames</span>();</div>
                            <div class="line"><span class="ln">5</span>  <span class="kw">const</span> video = <span class="kw">await</span> engine.<span class="fn">export</span>({</div>
                            <div class="line"><span class="ln">6</span>    {{PARAM_NAME}}</div>
                            <div class="line"><span class="ln">7</span>  });</div>
                            <div class="line"><span class="ln">8</span>  <span class="kw">return</span> video.<span class="fn">save</span>();</div>
                            <div class="line"><span class="ln">9</span>}</div>
                        </div>
                        <div class="status-bar">
                            <span>UTF-8</span>
                            <span>TypeScript</span>
                            <span class="ready-badge">● COMPILING</span>
                        </div>
                    </div>
                </div>
            """.trimIndent(),
            cssStyle = """
                .ide-container {
                    width: 100%;
                    height: 100%;
                    background: #0d1117;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    padding: 20px;
                    box-sizing: border-box;
                    font-family: "Fira Code", monospace, monospace;
                }
                .window-frame {
                    width: 100%;
                    max-width: 440px;
                    background: #161b22;
                    border: 1px solid #30363d;
                    border-radius: 16px;
                    box-shadow: 0 25px 50px rgba(0,0,0,0.6);
                    overflow: hidden;
                    transform: translateY(calc((1 - min(var(--progress) * 2, 1)) * 30px));
                    opacity: calc(min(var(--progress) * 2, 1));
                }
                .titlebar {
                    background: #0d1117;
                    padding: 12px 16px;
                    display: flex;
                    align-items: center;
                    border-bottom: 1px solid #30363d;
                }
                .window-dots {
                    display: flex;
                    gap: 7px;
                }
                .dot {
                    width: 10px;
                    height: 10px;
                    border-radius: 50%;
                }
                .dot.red { background: #ff5f56; }
                .dot.yellow { background: #ffbd2e; }
                .dot.green { background: #27c93f; }
                .file-tab {
                    color: #8b949e;
                    font-size: 12px;
                    margin-left: 20px;
                }
                .code-body {
                    padding: 18px 16px;
                    font-size: 13px;
                    line-height: 1.6;
                    color: #c9d1d9;
                }
                .line {
                    white-space: pre;
                    opacity: 0.95;
                }
                .ln {
                    color: #484f58;
                    margin-right: 14px;
                    display: inline-block;
                    width: 14px;
                }
                .kw { color: #ff7b72; font-weight: bold; }
                .fn { color: #d2a8ff; }
                .str { color: #a5d6ff; }
                .cls { color: #ffa657; }
                .status-bar {
                    background: #0d1117;
                    border-top: 1px solid #30363d;
                    padding: 8px 16px;
                    display: flex;
                    justify-content: space-between;
                    font-size: 11px;
                    color: #8b949e;
                }
                .ready-badge {
                    color: {{THEME_COLOR}};
                    font-weight: bold;
                }
            """.trimIndent(),
            jsScript = ""
        ),

        // 5. Generative Particle Canvas Wave
        VideoTemplate(
            id = "canvas_galaxy_wave",
            name = "Canvas Particle Galaxy",
            category = "Generative & Art",
            description = "Interactive HTML5 canvas generative wave math with glowing orbiting particles.",
            defaultDurationSec = 4.0f,
            defaultFps = 30,
            defaultAspectRatio = AspectRatioType.PORTRAIT_9_16,
            params = listOf(
                TemplateParam("TITLE", "Overlay Title", "SYNAPSE"),
                TemplateParam("SUBTITLE", "Subtitle", "Generative HTML5 Motion"),
                TemplateParam("COLOR_HEX", "Particle Glow", "#8B5CF6", type = ParamType.COLOR_HEX)
            ),
            htmlBody = """
                <div class="galaxy-box">
                    <canvas id="galaxyCanvas"></canvas>
                    <div class="overlay-text">
                        <h2>{{TITLE}}</h2>
                        <p>{{SUBTITLE}}</p>
                    </div>
                </div>
            """.trimIndent(),
            cssStyle = """
                .galaxy-box {
                    width: 100%;
                    height: 100%;
                    position: relative;
                    background: #05070e;
                    overflow: hidden;
                }
                #galaxyCanvas {
                    width: 100%;
                    height: 100%;
                    display: block;
                }
                .overlay-text {
                    position: absolute;
                    bottom: 40px;
                    left: 24px;
                    right: 24px;
                    text-align: center;
                    color: #fff;
                    z-index: 5;
                }
                .overlay-text h2 {
                    font-size: 32px;
                    font-weight: 800;
                    letter-spacing: 4px;
                    text-shadow: 0 0 20px {{COLOR_HEX}};
                }
                .overlay-text p {
                    font-size: 14px;
                    color: #94a3b8;
                    margin-top: 6px;
                }
            """.trimIndent(),
            jsScript = """
                const canvas = document.getElementById('galaxyCanvas');
                const ctx = canvas.getContext('2d');
                
                function resize() {
                    canvas.width = window.innerWidth * 2;
                    canvas.height = window.innerHeight * 2;
                }
                resize();
                window.addEventListener('resize', resize);

                function drawFrame(t, progress) {
                    const w = canvas.width;
                    const h = canvas.height;
                    ctx.fillStyle = '#05070e';
                    ctx.fillRect(0, 0, w, h);

                    const centerX = w / 2;
                    const centerY = h / 2;
                    const numParticles = 75;

                    for (let i = 0; i < numParticles; i++) {
                        const angle = (i / numParticles) * Math.PI * 2 + (progress * Math.PI * 4);
                        const radius = 100 + i * 4.5 + Math.sin(progress * 8 + i * 0.2) * 50;
                        const x = centerX + Math.cos(angle) * radius;
                        const y = centerY + Math.sin(angle) * (radius * 0.7);

                        const size = 3 + Math.sin(progress * 10 + i) * 2;
                        ctx.beginPath();
                        ctx.arc(x, y, Math.max(size, 1), 0, Math.PI * 2);
                        ctx.fillStyle = '{{COLOR_HEX}}';
                        ctx.shadowColor = '{{COLOR_HEX}}';
                        ctx.shadowBlur = 15;
                        ctx.fill();
                    }

                    // Sine Wave across center
                    ctx.beginPath();
                    for (let x = 0; x < w; x += 8) {
                        const y = centerY + Math.sin(x * 0.008 + progress * 12) * 80 * Math.sin(progress * Math.PI);
                        if (x === 0) ctx.moveTo(x, y);
                        else ctx.lineTo(x, y);
                    }
                    ctx.strokeStyle = '{{COLOR_HEX}}';
                    ctx.lineWidth = 4;
                    ctx.shadowBlur = 20;
                    ctx.shadowColor = '{{COLOR_HEX}}';
                    ctx.stroke();
                }

                window.onHyperFrameUpdate = function(t, progress) {
                    drawFrame(t, progress);
                };

                // Initial draw
                drawFrame(window.HyperFrames.time || 0, window.HyperFrames.progress || 0);
            """.trimIndent()
        ),

        // 6. Infographic Counter & Circle Gauge
        VideoTemplate(
            id = "stat_counter",
            name = "KPI Stat & Gauge",
            category = "Business & Stats",
            description = "Radial SVG progress meter, animated percentage counter, and KPI metric cards.",
            defaultDurationSec = 3.0f,
            defaultFps = 30,
            defaultAspectRatio = AspectRatioType.PORTRAIT_9_16,
            params = listOf(
                TemplateParam("METRIC_LABEL", "KPI Title", "PERFORMANCE"),
                TemplateParam("MAX_VAL", "Max Value", "98.4%"),
                TemplateParam("STAT_1", "Sub Metric 1", "+340% Growth"),
                TemplateParam("STAT_2", "Sub Metric 2", "0.2s Latency"),
                TemplateParam("ACCENT_COLOR", "Gauge Color", "#00F0FF", type = ParamType.COLOR_HEX)
            ),
            htmlBody = """
                <div class="kpi-container">
                    <div class="kpi-title">{{METRIC_LABEL}}</div>
                    
                    <div class="gauge-box">
                        <svg viewBox="0 0 100 100" class="gauge-svg">
                            <circle class="gauge-bg" cx="50" cy="50" r="42" />
                            <circle class="gauge-fill" cx="50" cy="50" r="42" />
                        </svg>
                        <div class="gauge-content">
                            <div class="gauge-val">{{MAX_VAL}}</div>
                            <div class="gauge-sub">EFFICIENCY</div>
                        </div>
                    </div>

                    <div class="stats-row">
                        <div class="stat-pill">{{STAT_1}}</div>
                        <div class="stat-pill">{{STAT_2}}</div>
                    </div>
                </div>
            """.trimIndent(),
            cssStyle = """
                .kpi-container {
                    width: 100%;
                    height: 100%;
                    background: radial-gradient(circle at 50% 30%, #151a30 0%, #070913 100%);
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                    padding: 30px;
                    box-sizing: border-box;
                    color: #fff;
                }
                .kpi-title {
                    font-size: 15px;
                    font-weight: 700;
                    letter-spacing: 4px;
                    color: #94a3b8;
                    margin-bottom: 30px;
                }
                .gauge-box {
                    position: relative;
                    width: 220px;
                    height: 220px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                }
                .gauge-svg {
                    width: 100%;
                    height: 100%;
                    transform: rotate(-90deg);
                }
                .gauge-bg {
                    fill: none;
                    stroke: rgba(255,255,255,0.08);
                    stroke-width: 8;
                }
                .gauge-fill {
                    fill: none;
                    stroke: {{ACCENT_COLOR}};
                    stroke-width: 8;
                    stroke-linecap: round;
                    stroke-dasharray: 264;
                    stroke-dashoffset: calc(264 * (1 - var(--progress)));
                    filter: drop-shadow(0 0 10px {{ACCENT_COLOR}});
                }
                .gauge-content {
                    position: absolute;
                    text-align: center;
                }
                .gauge-val {
                    font-size: 38px;
                    font-weight: 900;
                    color: #ffffff;
                }
                .gauge-sub {
                    font-size: 11px;
                    letter-spacing: 2px;
                    color: {{ACCENT_COLOR}};
                    margin-top: 4px;
                }
                .stats-row {
                    display: flex;
                    gap: 12px;
                    margin-top: 36px;
                }
                .stat-pill {
                    padding: 10px 18px;
                    background: rgba(255,255,255,0.06);
                    border: 1px solid rgba(255,255,255,0.12);
                    border-radius: 12px;
                    font-size: 13px;
                    font-weight: 600;
                    color: #e2e8f0;
                    transform: translateY(calc((1 - min(var(--progress) * 2, 1)) * 20px));
                    opacity: calc(min(var(--progress) * 2, 1));
                }
            """.trimIndent(),
            jsScript = ""
        )
    )
}
