import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';

export default function CNCMachine({ status = 'RUNNING', isHovered = false, isSelected = false }) {
  const spindleRef = useRef();
  const worklightRef = useRef();
  const isRunning = status === 'RUNNING';

  useFrame(({ clock }) => {
    if (isRunning && spindleRef.current) {
      const t = clock.getElapsedTime();
      // Gentle machining tool movement
      spindleRef.current.position.y = 1.05 + Math.sin(t * 3.5) * 0.12;
      spindleRef.current.position.x = Math.cos(t * 2.2) * 0.15;
    }
  });

  // Color scheme: Professional German/Japanese CNC Machine styling (Haas / DMG Mori / Mazak)
  const bodyColor = isSelected ? '#1e40af' : isHovered ? '#1e3a5f' : '#172554';
  const accentColor = '#38bdf8';
  const metalColor = '#334155';
  const baseColor = '#0f172a';

  return (
    <group position={[0, 0, 0]}>
      {/* 1. Heavy Base Casting / Foundation */}
      <mesh position={[0, 0.2, 0]} castShadow receiveShadow>
        <boxGeometry args={[2.4, 0.4, 2.2]} />
        <meshStandardMaterial color={baseColor} metalness={0.7} roughness={0.4} />
      </mesh>

      {/* Vibration Dampening Feet (4 corners) */}
      {[
        [-1.05, 0.05, -0.95],
        [1.05, 0.05, -0.95],
        [-1.05, 0.05, 0.95],
        [1.05, 0.05, 0.95],
      ].map((pos, i) => (
        <mesh key={i} position={pos} castShadow>
          <cylinderGeometry args={[0.1, 0.12, 0.1, 16]} />
          <meshStandardMaterial color="#475569" metalness={0.9} roughness={0.2} />
        </mesh>
      ))}

      {/* 2. Main Machine Enclosure Frame */}
      <mesh position={[0, 1.25, 0]} castShadow receiveShadow>
        <boxGeometry args={[2.2, 1.7, 2.0]} />
        <meshStandardMaterial
          color={bodyColor}
          metalness={0.5}
          roughness={0.35}
          emissive={isSelected ? '#1d4ed8' : '#000000'}
          emissiveIntensity={isSelected ? 0.35 : 0}
        />
      </mesh>

      {/* Decorative Accent Stripes */}
      <mesh position={[0, 2.05, 1.01]}>
        <boxGeometry args={[2.1, 0.06, 0.02]} />
        <meshStandardMaterial color={accentColor} emissive={accentColor} emissiveIntensity={0.6} roughness={0.3} />
      </mesh>
      <mesh position={[0, 0.45, 1.01]}>
        <boxGeometry args={[2.1, 0.06, 0.02]} />
        <meshStandardMaterial color={accentColor} emissive={accentColor} emissiveIntensity={0.6} roughness={0.3} />
      </mesh>

      {/* 3. Machining Interior Chamber (cutout recess) */}
      <mesh position={[0, 1.25, 0.2]}>
        <boxGeometry args={[1.6, 1.2, 1.3]} />
        <meshStandardMaterial color="#1e293b" metalness={0.8} roughness={0.5} />
      </mesh>

      {/* Chamber Internal Work Table / T-slot Bed */}
      <mesh position={[0, 0.75, 0.2]} castShadow receiveShadow>
        <boxGeometry args={[1.3, 0.2, 1.0]} />
        <meshStandardMaterial color="#64748b" metalness={0.9} roughness={0.2} />
      </mesh>

      {/* Clamped Raw Workpiece / Casting Billet */}
      <mesh position={[0, 0.92, 0.2]} castShadow>
        <cylinderGeometry args={[0.22, 0.25, 0.18, 16]} />
        <meshStandardMaterial color="#cbd5e1" metalness={0.95} roughness={0.15} />
      </mesh>

      {/* Moving CNC Spindle / Vertical Ram */}
      <group ref={spindleRef} position={[0, 1.2, 0.2]}>
        {/* Spindle Body */}
        <mesh position={[0, 0.25, 0]} castShadow>
          <cylinderGeometry args={[0.14, 0.16, 0.45, 16]} />
          <meshStandardMaterial color="#475569" metalness={0.9} roughness={0.2} />
        </mesh>
        {/* Tool Holder & Chuck */}
        <mesh position={[0, -0.02, 0]} castShadow>
          <cylinderGeometry args={[0.07, 0.11, 0.14, 16]} />
          <meshStandardMaterial color="#e2e8f0" metalness={0.95} roughness={0.1} />
        </mesh>
        {/* Tool Bit (Endmill / Casting nozzle) */}
        <mesh position={[0, -0.12, 0]}>
          <cylinderGeometry args={[0.025, 0.025, 0.1, 12]} />
          <meshStandardMaterial color="#f8fafc" metalness={1.0} roughness={0.05} />
        </mesh>
      </group>

      {/* Interior Chamber Work Light */}
      <pointLight
        ref={worklightRef}
        position={[0, 1.75, 0.3]}
        color={isRunning ? '#e0f2fe' : '#94a3b8'}
        intensity={isRunning ? 2.5 : 0.8}
        distance={2.5}
        decay={2}
      />

      {/* 4. Front Viewing Window (Tinted Polycarbonate Safety Glass) */}
      <mesh position={[0, 1.3, 1.02]}>
        <boxGeometry args={[1.5, 1.0, 0.04]} />
        <meshPhysicalMaterial
          color="#38bdf8"
          transmission={0.85}
          opacity={0.4}
          transparent
          roughness={0.1}
          metalness={0.1}
          ior={1.4}
        />
      </mesh>

      {/* Window Frame Bezel */}
      <mesh position={[0, 1.3, 1.01]}>
        <boxGeometry args={[1.58, 1.08, 0.02]} />
        <meshStandardMaterial color="#0f172a" metalness={0.8} roughness={0.3} />
      </mesh>

      {/* 5. Operator Control Console (Pendant Panel on Right Side) */}
      <group position={[1.25, 1.45, 0.8]} rotation={[0, -0.35, 0]}>
        {/* Support Pivot Arm */}
        <mesh position={[-0.15, -0.15, 0]}>
          <cylinderGeometry args={[0.04, 0.04, 0.45, 12]} />
          <meshStandardMaterial color="#475569" metalness={0.8} roughness={0.3} />
        </mesh>
        {/* Control Enclosure */}
        <mesh castShadow>
          <boxGeometry args={[0.42, 0.55, 0.12]} />
          <meshStandardMaterial color="#0f172a" metalness={0.7} roughness={0.3} />
        </mesh>
        {/* Glowing Touchscreen HMI */}
        <mesh position={[0, 0.08, 0.065]}>
          <planeGeometry args={[0.34, 0.28]} />
          <meshStandardMaterial
            color="#0284c7"
            emissive="#0284c7"
            emissiveIntensity={1.4}
            roughness={0.2}
          />
        </mesh>
        {/* Control Button Strip */}
        <mesh position={[0, -0.15, 0.065]}>
          <planeGeometry args={[0.32, 0.1]} />
          <meshStandardMaterial color="#334155" roughness={0.6} />
        </mesh>
        {/* Emergency Stop Button (E-Stop) */}
        <mesh position={[0.13, -0.15, 0.08]}>
          <cylinderGeometry args={[0.028, 0.028, 0.03, 16]} rotation={[Math.PI / 2, 0, 0]} />
          <meshStandardMaterial color="#ef4444" emissive="#ef4444" emissiveIntensity={0.8} roughness={0.2} />
        </mesh>
      </group>

      {/* 6. Top Roof Mounted Air Filtration & Exhaust Mist Collector */}
      <group position={[0, 2.25, -0.2]}>
        <mesh position={[0, 0.2, 0]} castShadow>
          <cylinderGeometry args={[0.35, 0.4, 0.45, 24]} />
          <meshStandardMaterial color="#475569" metalness={0.6} roughness={0.4} />
        </mesh>
        {/* Top Fan Grill */}
        <mesh position={[0, 0.44, 0]}>
          <cylinderGeometry args={[0.32, 0.32, 0.04, 24]} />
          <meshStandardMaterial color="#0f172a" metalness={0.9} roughness={0.2} />
        </mesh>
      </group>

      {/* 7. Ventilation Louvers on Rear/Side */}
      <mesh position={[-1.11, 1.4, 0]} rotation={[0, Math.PI / 2, 0]}>
        <planeGeometry args={[1.2, 0.8]} />
        <meshStandardMaterial color="#0f172a" metalness={0.8} roughness={0.4} />
      </mesh>
    </group>
  );
}

