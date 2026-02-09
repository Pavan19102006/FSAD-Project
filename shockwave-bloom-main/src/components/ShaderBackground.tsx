import { useRef, useMemo } from 'react';
import { Canvas, useFrame, useThree } from '@react-three/fiber';
import { 
  EffectComposer, 
  Bloom, 
  ChromaticAberration,
  Vignette,
  ToneMapping,
} from '@react-three/postprocessing';
import { BlendFunction, ToneMappingMode } from 'postprocessing';
import * as THREE from 'three';

const vertexShader = `
  varying vec2 vUv;
  varying vec3 vPosition;
  
  void main() {
    vUv = uv;
    vPosition = position;
    gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
  }
`;

// Liquid Gold Waves shader
const fragmentShader = `
  uniform float uTime;
  uniform vec2 uResolution;
  uniform vec2 uMouse;
  varying vec2 vUv;
  varying vec3 vPosition;

  #define PI 3.14159265359

  // Simplex noise functions
  vec3 mod289(vec3 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
  vec4 mod289(vec4 x) { return x - floor(x * (1.0 / 289.0)) * 289.0; }
  vec4 permute(vec4 x) { return mod289(((x*34.0)+1.0)*x); }
  vec4 taylorInvSqrt(vec4 r) { return 1.79284291400159 - 0.85373472095314 * r; }

  float snoise(vec3 v) {
    const vec2 C = vec2(1.0/6.0, 1.0/3.0);
    const vec4 D = vec4(0.0, 0.5, 1.0, 2.0);

    vec3 i  = floor(v + dot(v, C.yyy));
    vec3 x0 = v - i + dot(i, C.xxx);

    vec3 g = step(x0.yzx, x0.xyz);
    vec3 l = 1.0 - g;
    vec3 i1 = min(g.xyz, l.zxy);
    vec3 i2 = max(g.xyz, l.zxy);

    vec3 x1 = x0 - i1 + C.xxx;
    vec3 x2 = x0 - i2 + C.yyy;
    vec3 x3 = x0 - D.yyy;

    i = mod289(i);
    vec4 p = permute(permute(permute(
              i.z + vec4(0.0, i1.z, i2.z, 1.0))
            + i.y + vec4(0.0, i1.y, i2.y, 1.0))
            + i.x + vec4(0.0, i1.x, i2.x, 1.0));

    float n_ = 0.142857142857;
    vec3 ns = n_ * D.wyz - D.xzx;

    vec4 j = p - 49.0 * floor(p * ns.z * ns.z);

    vec4 x_ = floor(j * ns.z);
    vec4 y_ = floor(j - 7.0 * x_);

    vec4 x = x_ *ns.x + ns.yyyy;
    vec4 y = y_ *ns.x + ns.yyyy;
    vec4 h = 1.0 - abs(x) - abs(y);

    vec4 b0 = vec4(x.xy, y.xy);
    vec4 b1 = vec4(x.zw, y.zw);

    vec4 s0 = floor(b0)*2.0 + 1.0;
    vec4 s1 = floor(b1)*2.0 + 1.0;
    vec4 sh = -step(h, vec4(0.0));

    vec4 a0 = b0.xzyw + s0.xzyw*sh.xxyy;
    vec4 a1 = b1.xzyw + s1.xzyw*sh.zzww;

    vec3 p0 = vec3(a0.xy, h.x);
    vec3 p1 = vec3(a0.zw, h.y);
    vec3 p2 = vec3(a1.xy, h.z);
    vec3 p3 = vec3(a1.zw, h.w);

    vec4 norm = taylorInvSqrt(vec4(dot(p0,p0), dot(p1,p1), dot(p2,p2), dot(p3,p3)));
    p0 *= norm.x;
    p1 *= norm.y;
    p2 *= norm.z;
    p3 *= norm.w;

    vec4 m = max(0.6 - vec4(dot(x0,x0), dot(x1,x1), dot(x2,x2), dot(x3,x3)), 0.0);
    m = m * m;
    return 42.0 * dot(m*m, vec4(dot(p0,x0), dot(p1,x1), dot(p2,x2), dot(p3,x3)));
  }

  // FBM for layered noise
  float fbm(vec3 p) {
    float value = 0.0;
    float amplitude = 0.5;
    float frequency = 1.0;
    
    for(int i = 0; i < 5; i++) {
      value += amplitude * snoise(p * frequency);
      amplitude *= 0.5;
      frequency *= 2.0;
    }
    return value;
  }

  void main() {
    vec2 uv = vUv;
    float aspect = uResolution.x / uResolution.y;
    vec2 scaledUv = (uv - 0.5) * vec2(aspect, 1.0);
    
    float time = uTime * 0.2;
    
    // Create flowing liquid surface with more layers
    vec3 noisePos = vec3(scaledUv * 1.5, time * 0.3);
    
    // Multiple wave layers at different scales and speeds
    float wave1 = snoise(noisePos * 1.2 + vec3(0.0, 0.0, time * 0.15)) * 1.0;
    float wave2 = snoise(noisePos * 2.0 + vec3(time * 0.08, 0.0, 0.0)) * 0.5;
    float wave3 = snoise(noisePos * 3.5 + vec3(0.0, time * 0.1, time * 0.05)) * 0.25;
    float wave4 = snoise(noisePos * 6.0 + vec3(time * 0.12, time * 0.08, 0.0)) * 0.125;
    float wave5 = fbm(noisePos * 0.8 + vec3(time * 0.03)) * 0.4;
    
    float waves = wave1 + wave2 + wave3 + wave4 + wave5;
    
    // Mouse ripple effect - smoother
    vec2 mousePos = uMouse;
    float mouseDist = length(scaledUv - (mousePos - 0.5) * vec2(aspect, 1.0));
    float mouseRipple = sin(mouseDist * 12.0 - time * 5.0) * exp(-mouseDist * 2.5) * 0.25;
    waves += mouseRipple;
    
    // Calculate surface normal for lighting - more precise
    float epsilon = 0.008;
    float dx = snoise(vec3((scaledUv.x + epsilon) * 1.5, scaledUv.y * 1.5, time * 0.3)) - 
               snoise(vec3((scaledUv.x - epsilon) * 1.5, scaledUv.y * 1.5, time * 0.3));
    float dy = snoise(vec3(scaledUv.x * 1.5, (scaledUv.y + epsilon) * 1.5, time * 0.3)) - 
               snoise(vec3(scaledUv.x * 1.5, (scaledUv.y - epsilon) * 1.5, time * 0.3));
    
    vec3 normal = normalize(vec3(-dx * 3.0, -dy * 3.0, 1.0));
    
    // Multiple light sources for richer lighting
    vec3 lightDir1 = normalize(vec3(0.6, 0.8, 1.0));  // Main light
    vec3 lightDir2 = normalize(vec3(-0.5, 0.3, 0.8)); // Fill light
    vec3 lightDir3 = normalize(vec3(0.0, -0.5, 0.6)); // Rim light
    vec3 viewDir = vec3(0.0, 0.0, 1.0);
    
    // Main light calculations
    float diffuse1 = max(dot(normal, lightDir1), 0.0);
    vec3 halfDir1 = normalize(lightDir1 + viewDir);
    float specular1 = pow(max(dot(normal, halfDir1), 0.0), 128.0);
    
    // Fill light (softer, cooler)
    float diffuse2 = max(dot(normal, lightDir2), 0.0) * 0.3;
    vec3 halfDir2 = normalize(lightDir2 + viewDir);
    float specular2 = pow(max(dot(normal, halfDir2), 0.0), 64.0) * 0.4;
    
    // Rim light
    float rim = pow(1.0 - max(dot(normal, viewDir), 0.0), 4.0);
    
    // Combined lighting
    float diffuse = diffuse1 + diffuse2;
    float specular = specular1 + specular2;
    
    // Premium color palette
    vec3 voidBlack = vec3(0.0, 0.0, 0.0);
    vec3 deepShadow = vec3(0.015, 0.008, 0.002);
    vec3 darkGold = vec3(0.15, 0.08, 0.01);
    vec3 richGold = vec3(0.6, 0.4, 0.05);
    vec3 brightGold = vec3(0.95, 0.75, 0.2);
    vec3 hotGold = vec3(1.0, 0.9, 0.5);
    vec3 copper = vec3(0.4, 0.2, 0.05);
    vec3 amber = vec3(0.7, 0.35, 0.05);
    
    // Height-based color with strong contrast
    float heightFactor = (waves + 1.5) * 0.35;
    heightFactor = clamp(heightFactor, 0.0, 1.0);
    heightFactor = pow(heightFactor, 2.0); // Strong contrast curve
    
    // Build base color from shadows to highlights
    vec3 baseColor = mix(voidBlack, deepShadow, smoothstep(0.0, 0.1, heightFactor));
    baseColor = mix(baseColor, darkGold, smoothstep(0.1, 0.3, heightFactor));
    baseColor = mix(baseColor, copper, smoothstep(0.25, 0.5, heightFactor) * 0.5);
    baseColor = mix(baseColor, richGold, smoothstep(0.4, 0.7, heightFactor));
    
    // Apply diffuse lighting
    baseColor = mix(baseColor * 0.3, baseColor, pow(diffuse, 0.8));
    
    // Color temperature variation
    float tempVar = snoise(noisePos * 2.0 + vec3(time * 0.1)) * 0.5 + 0.5;
    baseColor = mix(baseColor, baseColor * vec3(1.0, 0.85, 0.7), tempVar * 0.2);
    
    // Specular highlights - sharp and brilliant
    vec3 specColor = mix(brightGold, hotGold, specular);
    baseColor += specColor * pow(specular, 1.2) * 1.5;
    
    // Secondary specular for depth
    float microSpecular = pow(max(dot(normal, normalize(vec3(0.3, 0.6, 1.0))), 0.0), 256.0);
    baseColor += hotGold * microSpecular * 0.8;
    
    // Rim/fresnel lighting
    baseColor += amber * rim * 0.25;
    
    // Shimmer effect on highlights
    float shimmer = snoise(vec3(scaledUv * 20.0, time * 2.0));
    shimmer = smoothstep(0.7, 1.0, shimmer) * specular;
    baseColor += hotGold * shimmer * 0.3;
    
    // Ambient occlusion in crevices
    float ao = smoothstep(-0.5, 0.5, waves);
    baseColor *= mix(0.4, 1.0, ao);
    
    // Subtle caustic sparkles
    float sparkle = snoise(vec3(scaledUv * 30.0, time * 1.5));
    sparkle = pow(max(sparkle, 0.0), 8.0);
    baseColor += hotGold * sparkle * 0.15 * heightFactor;
    
    // Deep vignette
    vec2 vignetteUv = uv * (1.0 - uv);
    float vignette = vignetteUv.x * vignetteUv.y * 18.0;
    vignette = pow(vignette, 0.6);
    baseColor *= vignette;
    
    // Final contrast and tone curve
    baseColor = baseColor * baseColor * (3.0 - 2.0 * baseColor); // S-curve
    baseColor = pow(baseColor, vec3(0.95)); // Slight gamma
    
    // Subtle color grading - warm shadows, bright highlights
    baseColor.r *= 1.02;
    baseColor.b *= 0.95;
    
    gl_FragColor = vec4(baseColor, 1.0);
  }
`;

function LiquidPlane() {
  const meshRef = useRef<THREE.Mesh>(null);
  const { viewport, size } = useThree();
  const mouse = useRef(new THREE.Vector2(0.5, 0.5));

  const uniforms = useMemo(
    () => ({
      uTime: { value: 0 },
      uResolution: { value: new THREE.Vector2(size.width, size.height) },
      uMouse: { value: new THREE.Vector2(0.5, 0.5) },
    }),
    [size]
  );

  // Mouse tracking
  useMemo(() => {
    const handleMouseMove = (e: MouseEvent) => {
      mouse.current.set(
        e.clientX / window.innerWidth,
        1.0 - e.clientY / window.innerHeight
      );
    };
    window.addEventListener('mousemove', handleMouseMove);
    return () => window.removeEventListener('mousemove', handleMouseMove);
  }, []);

  useFrame((state) => {
    if (meshRef.current) {
      const material = meshRef.current.material as THREE.ShaderMaterial;
      material.uniforms.uTime.value = state.clock.elapsedTime;
      
      // Smooth mouse following
      material.uniforms.uMouse.value.lerp(mouse.current, 0.05);
    }
  });

  return (
    <mesh ref={meshRef} position={[0, 0, -2]} scale={[viewport.width * 2, viewport.height * 2, 1]}>
      <planeGeometry args={[1, 1, 1, 1]} />
      <shaderMaterial
        vertexShader={vertexShader}
        fragmentShader={fragmentShader}
        uniforms={uniforms}
      />
    </mesh>
  );
}

// Floating gold droplets
function GoldDroplets() {
  const groupRef = useRef<THREE.Group>(null);
  
  const droplets = useMemo(() => {
    return Array.from({ length: 15 }, (_, i) => ({
      position: [
        (Math.random() - 0.5) * 8,
        (Math.random() - 0.5) * 5,
        -1 - Math.random() * 3,
      ] as [number, number, number],
      scale: 0.05 + Math.random() * 0.1,
      speed: 0.5 + Math.random() * 1.5,
      offset: Math.random() * Math.PI * 2,
    }));
  }, []);

  useFrame((state) => {
    if (groupRef.current) {
      groupRef.current.children.forEach((child, i) => {
        const droplet = droplets[i];
        child.position.y = droplet.position[1] + Math.sin(state.clock.elapsedTime * droplet.speed + droplet.offset) * 0.3;
        child.position.x = droplet.position[0] + Math.cos(state.clock.elapsedTime * droplet.speed * 0.5 + droplet.offset) * 0.2;
      });
    }
  });

  return (
    <group ref={groupRef}>
      {droplets.map((droplet, i) => (
        <mesh key={i} position={droplet.position} scale={droplet.scale}>
          <sphereGeometry args={[1, 16, 16]} />
          <meshStandardMaterial
            color="#ffd700"
            emissive="#d4a418"
            emissiveIntensity={0.8}
            roughness={0.1}
            metalness={1}
          />
        </mesh>
      ))}
    </group>
  );
}

function Scene() {
  return (
    <>
      {/* Refined lighting setup */}
      <ambientLight intensity={0.08} />
      <pointLight position={[6, 6, 6]} intensity={0.6} color="#ffd700" />
      <pointLight position={[-4, 4, 3]} intensity={0.3} color="#b8860b" />
      <pointLight position={[0, -3, 4]} intensity={0.2} color="#cd853f" />
      <spotLight
        position={[0, 10, 5]}
        angle={0.4}
        penumbra={1}
        intensity={0.4}
        color="#ffe066"
      />

      {/* Liquid gold surface */}
      <LiquidPlane />

      {/* Floating droplets */}
      <GoldDroplets />

      {/* Post-processing - premium cinematic */}
      <EffectComposer multisampling={8}>
        <Bloom
          intensity={0.8}
          luminanceThreshold={0.65}
          luminanceSmoothing={0.6}
          mipmapBlur
        />
        <ChromaticAberration
          blendFunction={BlendFunction.NORMAL}
          offset={new THREE.Vector2(0.0005, 0.0005)}
          radialModulation={true}
          modulationOffset={0.15}
        />
        <Vignette
          offset={0.2}
          darkness={0.85}
          blendFunction={BlendFunction.NORMAL}
        />
        <ToneMapping mode={ToneMappingMode.ACES_FILMIC} />
      </EffectComposer>
    </>
  );
}

export default function ShaderBackground() {
  return (
    <div className="fixed inset-0 -z-10">
      <Canvas
        camera={{ position: [0, 0, 3], fov: 55 }}
        gl={{ antialias: true, alpha: false, powerPreference: 'high-performance' }}
        dpr={[1, 2]}
      >
        <Scene />
      </Canvas>
    </div>
  );
}
