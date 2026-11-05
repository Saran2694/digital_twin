import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';

export default function ConveyorBelt({
  position = [0, 0, 0],
  length = 18,
  width = 0.8,
  height = 0.65,
  speed = 1.2,
  itemCount = 6,
}) {
  const itemsRef = useRef([]);

  useFrame((_, delta) => {
    const halfLen = length / 2;
    // Advance each part smoothly along the conveyor
    itemsRef.current.forEach((item) => {
      if (!item) return;
      item.position.x += speed * delta;
      if (item.position.x > halfLen) {
        item.position.x = -halfLen;
      }
    });
  });

  const legCount = Math.floor(length / 3) + 1;
  const legSpacing = length / (legCount - 1);

  return (
    <group position={position}>
      {/* 1. Main Conveyor Belt Bed */}
      <mesh position={[0, height, 0]} castShadow receiveShadow>
        <boxGeometry args={[length, 0.08, width]} />
        <meshStandardMaterial color="#0f172a" roughness={0.8} metalness={0.2} />
      </mesh>

      {/* Belt Guide Rails (Side Edges) */}
      <mesh position={[0, height + 0.08, width / 2 + 0.02]} castShadow>
        <boxGeometry args={[length, 0.12, 0.04]} />
        <meshStandardMaterial color="#38bdf8" metalness={0.8} roughness={0.3} />
      </mesh>
      <mesh position={[0, height + 0.08, -width / 2 - 0.02]} castShadow>
        <boxGeometry args={[length, 0.12, 0.04]} />
        <meshStandardMaterial color="#38bdf8" metalness={0.8} roughness={0.3} />
      </mesh>

      {/* Structural Support Legs & Cross Beams */}
      {Array.from({ length: legCount }).map((_, i) => {
        const xPos = -length / 2 + i * legSpacing;
        return (
          <group key={i} position={[xPos, 0, 0]}>
            {/* Front Leg */}
            <mesh position={[0, height / 2, width / 2]} castShadow>
              <cylinderGeometry args={[0.04, 0.04, height, 12]} />
              <meshStandardMaterial color="#334155" metalness={0.8} roughness={0.3} />
            </mesh>
            {/* Rear Leg */}
            <mesh position={[0, height / 2, -width / 2]} castShadow>
              <cylinderGeometry args={[0.04, 0.04, height, 12]} />
              <meshStandardMaterial color="#334155" metalness={0.8} roughness={0.3} />
            </mesh>
            {/* Cross Tie */}
            <mesh position={[0, height * 0.4, 0]} rotation={[Math.PI / 2, 0, 0]}>
              <cylinderGeometry args={[0.025, 0.025, width, 12]} />
              <meshStandardMaterial color="#475569" metalness={0.8} roughness={0.3} />
            </mesh>
            {/* Floor Anchor Footing */}
            <mesh position={[0, 0.02, width / 2]}>
              <boxGeometry args={[0.12, 0.04, 0.12]} />
              <meshStandardMaterial color="#1e293b" metalness={0.9} roughness={0.2} />
            </mesh>
            <mesh position={[0, 0.02, -width / 2]}>
              <boxGeometry args={[0.12, 0.04, 0.12]} />
              <meshStandardMaterial color="#1e293b" metalness={0.9} roughness={0.2} />
            </mesh>
          </group>
        );
      })}

      {/* 2. Moving Industrial Parts / Casting Billets along Conveyor */}
      {Array.from({ length: itemCount }).map((_, i) => {
        const initX = -length / 2 + (i * length) / itemCount;
        const isBillet = i % 2 === 0;

        return (
          <group
            key={i}
            ref={(el) => (itemsRef.current[i] = el)}
            position={[initX, height + 0.12, 0]}
          >
            {/* Pallet Carrier Tray */}
            <mesh castShadow receiveShadow position={[0, -0.04, 0]}>
              <boxGeometry args={[0.45, 0.04, width * 0.72]} />
              <meshStandardMaterial color="#1e293b" metalness={0.5} roughness={0.4} />
            </mesh>

            {/* Carried Part */}
            {isBillet ? (
              <mesh castShadow position={[0, 0.08, 0]}>
                <cylinderGeometry args={[0.12, 0.12, 0.22, 16]} rotation={[0, 0, Math.PI / 2]} />
                <meshStandardMaterial color="#94a3b8" metalness={0.9} roughness={0.2} />
              </mesh>
            ) : (
              <mesh castShadow position={[0, 0.08, 0]}>
                <boxGeometry args={[0.26, 0.18, 0.26]} />
                <meshStandardMaterial color="#e2e8f0" metalness={0.85} roughness={0.25} />
              </mesh>
            )}
          </group>
        );
      })}

      {/* Optical Sensor Gate with Indicator LED */}
      <group position={[length * 0.2, height + 0.2, 0]}>
        <mesh position={[0, 0, width / 2 + 0.06]}>
          <boxGeometry args={[0.08, 0.24, 0.08]} />
          <meshStandardMaterial color="#0f172a" />
        </mesh>
        <mesh position={[0, 0, -width / 2 - 0.06]}>
          <boxGeometry args={[0.08, 0.24, 0.08]} />
          <meshStandardMaterial color="#0f172a" />
        </mesh>
        {/* Sensor Beam */}
        <mesh position={[0, 0, 0]} rotation={[Math.PI / 2, 0, 0]}>
          <cylinderGeometry args={[0.006, 0.006, width + 0.1, 8]} />
          <meshBasicMaterial color="#ef4444" transparent opacity={0.6} />
        </mesh>
        <pointLight position={[0, 0.1, width / 2 + 0.06]} color="#22c55e" intensity={0.5} distance={1} />
      </group>
    </group>
  );
}

