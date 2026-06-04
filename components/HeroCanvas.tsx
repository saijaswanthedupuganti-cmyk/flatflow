'use client'

import { Suspense, useRef, useMemo } from 'react'
import { Canvas, useFrame } from '@react-three/fiber'
import { Float } from '@react-three/drei'
import * as THREE from 'three'

function useMats() {
  return useMemo(() => ({
    core:   new THREE.MeshStandardMaterial({ color: '#7c3aed', emissive: '#4c1d95', emissiveIntensity: 0.7, roughness: 0.1, metalness: 0.95, flatShading: true }),
    mid:    new THREE.MeshStandardMaterial({ color: '#4338ca', emissive: '#312e81', emissiveIntensity: 0.5, roughness: 0.2, metalness: 0.85, flatShading: true }),
    glow:   new THREE.MeshStandardMaterial({ color: '#a78bfa', emissive: '#7c3aed', emissiveIntensity: 1.4, roughness: 0.05, metalness: 1.0,  flatShading: true }),
    card:   new THREE.MeshStandardMaterial({ color: '#1e1b4b', emissive: '#4338ca', emissiveIntensity: 0.35, roughness: 0.4, metalness: 0.6 }),
    accent: new THREE.MeshStandardMaterial({ color: '#6366f1', emissive: '#4f46e5', emissiveIntensity: 0.9, roughness: 0.1, metalness: 0.9, flatShading: true }),
  }), [])
}

// ── Central hub ───────────────────────────────────────────────────────────────
function Hub() {
  const g = useRef<THREE.Group>(null!)
  const { core, glow } = useMats()
  useFrame(({ clock: c }) => {
    g.current.rotation.y = c.getElapsedTime() * 0.18
    g.current.rotation.x = Math.sin(c.getElapsedTime() * 0.35) * 0.07
  })
  return (
    <group ref={g}>
      <mesh material={core}><icosahedronGeometry args={[0.78, 1]} /></mesh>
      {/* Glow shell */}
      <mesh material={new THREE.MeshStandardMaterial({ color: '#a78bfa', emissive: '#7c3aed', emissiveIntensity: 0.9, transparent: true, opacity: 0.12, side: THREE.BackSide })}>
        <icosahedronGeometry args={[1.05, 1]} />
      </mesh>
      {/* Equatorial ring */}
      <mesh rotation={[Math.PI / 2, 0, 0]} material={glow}><torusGeometry args={[1.12, 0.02, 8, 120]} /></mesh>
      {/* Tilted ring */}
      <mesh rotation={[Math.PI / 3, 0.6, 0]} material={new THREE.MeshStandardMaterial({ color: '#818cf8', emissive: '#818cf8', emissiveIntensity: 1.2, transparent: true, opacity: 0.3 })}>
        <torusGeometry args={[1.12, 0.01, 8, 120]} />
      </mesh>
    </group>
  )
}

// ── Orbit rings ───────────────────────────────────────────────────────────────
function Ring({ r, tilt = 0, op = 0.25 }: { r: number; tilt?: number; op?: number }) {
  return (
    <mesh rotation={[Math.PI / 2 + tilt, 0, 0]}
      material={new THREE.MeshStandardMaterial({ color: '#7c3aed', emissive: '#7c3aed', emissiveIntensity: 1.0, transparent: true, opacity: op })}>
      <torusGeometry args={[r, 0.009, 8, 200]} />
    </mesh>
  )
}

// ── Orbiters (member spheres) ─────────────────────────────────────────────────
const ORB_DEF = [
  { offset: 0,             speed: 0.40, size: 0.24, mk: 'glow'   as const },
  { offset: Math.PI*.5,   speed: 0.40, size: 0.20, mk: 'mid'    as const },
  { offset: Math.PI,      speed: 0.40, size: 0.22, mk: 'core'   as const },
  { offset: Math.PI*1.5, speed: 0.40, size: 0.19, mk: 'accent' as const },
]

function Orbiter({ offset, speed, size, mk }: typeof ORB_DEF[0]) {
  const m = useRef<THREE.Mesh>(null!)
  const mats = useMats()
  const R = 2.55
  useFrame(({ clock: c }) => {
    const a = c.getElapsedTime() * speed + offset
    m.current.position.set(Math.cos(a) * R, 0, Math.sin(a) * R)
    m.current.rotation.y = c.getElapsedTime() * 2.5
    m.current.rotation.x = c.getElapsedTime() * 1.8
    m.current.scale.setScalar(1 + Math.sin(c.getElapsedTime() * 2 + offset) * 0.09)
  })
  return <mesh ref={m} material={mats[mk]}><icosahedronGeometry args={[size, 0]} /></mesh>
}

// ── Duty cards (flat boxes) ───────────────────────────────────────────────────
const CARD_DEF = [
  { p: [ 3.4,  0.9,  0.6] as [number,number,number], r: [ 0.3,  0.5,  0.2] as [number,number,number], s: 1.3 },
  { p: [-3.2,  0.5, -0.9] as [number,number,number], r: [ 0.1, -0.4,  0.3] as [number,number,number], s: 1.6 },
  { p: [ 0.6,  3.0,  1.3] as [number,number,number], r: [ 0.5,  0.2, -0.3] as [number,number,number], s: 1.1 },
  { p: [-0.7, -2.8,  1.0] as [number,number,number], r: [-0.2,  0.6,  0.4] as [number,number,number], s: 1.8 },
  { p: [ 2.2, -1.8, -1.3] as [number,number,number], r: [ 0.4, -0.3,  0.5] as [number,number,number], s: 1.4 },
  { p: [-1.5,  2.2, -0.6] as [number,number,number], r: [-0.3,  0.5, -0.2] as [number,number,number], s: 1.2 },
]

function DutyCard({ p, r, s }: typeof CARD_DEF[0]) {
  const { card, glow, accent } = useMats()
  return (
    <Float speed={s} floatIntensity={0.55} rotationIntensity={0.35}>
      <group position={p} rotation={r}>
        <mesh material={card}><boxGeometry args={[0.78, 0.46, 0.055]} /></mesh>
        <mesh position={[-0.28, 0, 0.03]} material={accent}><boxGeometry args={[0.065, 0.34, 0.012]} /></mesh>
        <mesh position={[0.24, 0.09, 0.03]} material={new THREE.MeshStandardMaterial({ color: '#10b981', emissive: '#10b981', emissiveIntensity: 1.5 })}>
          <circleGeometry args={[0.055, 14]} />
        </mesh>
        <mesh position={[0.02, -0.02, 0.03]} material={new THREE.MeshStandardMaterial({ color: '#a78bfa', emissive: '#a78bfa', emissiveIntensity: 0.6, transparent: true, opacity: 0.5 })}>
          <boxGeometry args={[0.32, 0.055, 0.008]} />
        </mesh>
        <mesh position={[0.02, -0.1, 0.03]} material={new THREE.MeshStandardMaterial({ color: '#6366f1', emissive: '#6366f1', emissiveIntensity: 0.4, transparent: true, opacity: 0.35 })}>
          <boxGeometry args={[0.22, 0.04, 0.008]} />
        </mesh>
      </group>
    </Float>
  )
}

// ── Particles ─────────────────────────────────────────────────────────────────
function Particles({ n = 220 }: { n?: number }) {
  const pts = useRef<THREE.Points>(null!)
  const [pos, vel] = useMemo(() => {
    const p = new Float32Array(n * 3)
    const v = new Float32Array(n)
    for (let i = 0; i < n; i++) {
      p[i*3]   = (Math.random() - 0.5) * 20
      p[i*3+1] = (Math.random() - 0.5) * 13
      p[i*3+2] = (Math.random() - 0.5) * 9
      v[i] = 0.002 + Math.random() * 0.004
    }
    return [p, v]
  }, [n])

  useFrame(() => {
    const a = pts.current.geometry.attributes.position
    const arr = a.array as Float32Array
    for (let i = 0; i < n; i++) { arr[i*3+1] += vel[i]; if (arr[i*3+1] > 6.5) arr[i*3+1] = -6.5 }
    a.needsUpdate = true
  })

  return (
    <points ref={pts}>
      <bufferGeometry>
        <bufferAttribute attach="attributes-position" args={[pos, 3]} count={n} array={pos} itemSize={3} />
      </bufferGeometry>
      <pointsMaterial size={0.032} color="#7c3aed" transparent opacity={0.45} sizeAttenuation depthWrite={false} />
    </points>
  )
}

// ── Scene ─────────────────────────────────────────────────────────────────────
function Scene({ mouse }: { mouse: React.MutableRefObject<[number, number]> }) {
  const root = useRef<THREE.Group>(null!)
  useFrame(() => {
    const [mx, my] = mouse.current
    root.current.rotation.y += (mx * 0.14 - root.current.rotation.y) * 0.03
    root.current.rotation.x += (-my * 0.09 - root.current.rotation.x) * 0.03
  })
  return (
    <>
      <ambientLight intensity={0.12} color="#1e1b4b" />
      <pointLight position={[ 6, 7,  6]} intensity={90}  color="#7c3aed" />
      <pointLight position={[-6, 4, -5]} intensity={60}  color="#4338ca" />
      <pointLight position={[ 0,-5,  5]} intensity={40}  color="#a78bfa" />
      <pointLight position={[ 0, 9,  0]} intensity={28}  color="#c4b5fd" />
      <Particles />
      <group ref={root}>
        <Hub />
        <Ring r={2.55} op={0.28} />
        <Ring r={2.55} tilt={0.22} op={0.1} />
        {ORB_DEF.map((o, i) => <Orbiter key={i} {...o} />)}
        {CARD_DEF.map((c, i) => <DutyCard key={i} {...c} />)}
      </group>
      <mesh visible={false} position={[0, 0, 4]}
        onPointerMove={e => { mouse.current = [(e.uv!.x-.5)*2, (e.uv!.y-.5)*2] }}>
        <planeGeometry args={[44, 28]} />
        <meshBasicMaterial />
      </mesh>
    </>
  )
}

export default function HeroCanvas() {
  const mouse = useRef<[number, number]>([0, 0])
  return (
    <Canvas camera={{ position: [1.2, 0.6, 8.0], fov: 48 }} dpr={[1, 1.5]} gl={{ antialias: true }}>
      <color attach="background" args={['#0a0a0a']} />
      <fog attach="fog" args={['#0a0a0a', 15, 28]} />
      <Suspense fallback={null}>
        <Scene mouse={mouse} />
      </Suspense>
    </Canvas>
  )
}
