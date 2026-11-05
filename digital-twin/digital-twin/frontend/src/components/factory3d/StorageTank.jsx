export default function StorageTank({
  position = [0, 0, 0],
  radius = 1.1,
  height = 3.6,
  color = '#1e3a5f',
  label = 'COOLANT-1',
}) {
  return (
    <group position={position}>
      {/* 1. Main Cylindrical Tank Body */}
      <mesh position={[0, height / 2 + 0.8, 0]} castShadow receiveShadow>
        <cylinderGeometry args={[radius, radius, height, 32]} />
        <meshStandardMaterial color={color} metalness={0.7} roughness={0.3} />
      </mesh>

      {/* Dished Top Head (Hemisphere) */}
      <mesh position={[0, height + 0.8, 0]} castShadow>
        <sphereGeometry args={[radius, 32, 16, 0, Math.PI * 2, 0, Math.PI / 2]} />
        <meshStandardMaterial color={color} metalness={0.7} roughness={0.3} />
      </mesh>

      {/* Dished Bottom Head */}
      <mesh position={[0, 0.8, 0]} rotation={[Math.PI, 0, 0]} castShadow>
        <sphereGeometry args={[radius, 32, 16, 0, Math.PI * 2, 0, Math.PI / 2]} />
        <meshStandardMaterial color={color} metalness={0.7} roughness={0.3} />
      </mesh>

      {/* Tank Circumference Reinforcing Rings */}
      {[-0.6, 0.6].map((offset, i) => (
        <mesh key={i} position={[0, height / 2 + 0.8 + offset, 0]}>
          <torusGeometry args={[radius + 0.03, 0.035, 12, 32]} rotation={[Math.PI / 2, 0, 0]} />
          <meshStandardMaterial color="#38bdf8" metalness={0.8} roughness={0.3} />
        </mesh>
      ))}

      {/* 2. Heavy Support Legs (4 legs) */}
      {[0, Math.PI / 2, Math.PI, (Math.PI * 3) / 2].map((angle, i) => {
        const x = Math.cos(angle) * (radius * 0.9);
        const z = Math.sin(angle) * (radius * 0.9);
        return (
          <group key={i} position={[x, 0, z]}>
            <mesh position={[0, 0.45, 0]} castShadow>
              <boxGeometry args={[0.15, 0.9, 0.15]} />
              <meshStandardMaterial color="#0f172a" metalness={0.8} roughness={0.4} />
            </mesh>
            {/* Footpad */}
            <mesh position={[0, 0.02, 0]}>
              <boxGeometry args={[0.3, 0.04, 0.3]} />
              <meshStandardMaterial color="#334155" metalness={0.9} roughness={0.3} />
            </mesh>
          </group>
        );
      })}

      {/* 3. Level Sight Gauge (Vertical tube on front of tank) */}
      <group position={[0, height / 2 + 0.8, radius + 0.08]}>
        {/* Sight Glass Tube */}
        <mesh>
          <cylinderGeometry args={[0.04, 0.04, height * 0.7, 16]} />
          <meshPhysicalMaterial color="#38bdf8" transmission={0.9} roughness={0.1} transparent opacity={0.6} />
        </mesh>
        {/* Fluid Level inside */}
        <mesh position={[0, -height * 0.1, 0]}>
          <cylinderGeometry args={[0.03, 0.03, height * 0.45, 16]} />
          <meshStandardMaterial color="#0284c7" emissive="#0284c7" emissiveIntensity={0.8} />
        </mesh>
      </group>

      {/* 4. Top Inspection Manhole & Pressure Valve */}
      <mesh position={[0, height + 0.8 + radius * 0.95, 0]} castShadow>
        <cylinderGeometry args={[0.32, 0.32, 0.18, 20]} />
        <meshStandardMaterial color="#334155" metalness={0.8} roughness={0.3} />
      </mesh>
      {/* Pressure Relief Valve */}
      <mesh position={[0.4, height + 0.8 + radius * 0.85, 0]} castShadow>
        <cylinderGeometry args={[0.06, 0.06, 0.3, 12]} />
        <meshStandardMaterial color="#eab308" metalness={0.8} roughness={0.3} />
      </mesh>

      {/* 5. Fluid Connection Piping to Factory Floor */}
      <group position={[radius, 0.5, 0]}>
        <mesh position={[0.35, 0, 0]} rotation={[0, 0, Math.PI / 2]} castShadow>
          <cylinderGeometry args={[0.08, 0.08, 0.8, 16]} />
          <meshStandardMaterial color="#475569" metalness={0.8} roughness={0.3} />
        </mesh>
        {/* Pipe Flange Coupling */}
        <mesh position={[0.7, 0, 0]} rotation={[0, 0, Math.PI / 2]}>
          <cylinderGeometry args={[0.13, 0.13, 0.06, 16]} />
          <meshStandardMaterial color="#64748b" metalness={0.9} roughness={0.2} />
        </mesh>
        {/* Vertical drop pipe to trench */}
        <mesh position={[0.7, -0.25, 0]} castShadow>
          <cylinderGeometry args={[0.08, 0.08, 0.5, 16]} />
          <meshStandardMaterial color="#475569" metalness={0.8} roughness={0.3} />
        </mesh>
      </group>
    </group>
  );
}

