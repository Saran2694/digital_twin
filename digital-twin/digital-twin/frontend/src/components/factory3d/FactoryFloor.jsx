export default function FactoryFloor({
  width = 38,
  depth = 28,
}) {
  return (
    <group position={[0, 0, 0]}>
      {/* 1. Main Industrial Concrete Floor Slab */}
      <mesh position={[0, -0.05, 0]} receiveShadow>
        <boxGeometry args={[width, 0.1, depth]} />
        <meshStandardMaterial
          color="#0b172a"
          roughness={0.7}
          metalness={0.25}
        />
      </mesh>

      {/* Grid Lines across the factory floor */}
      <gridHelper
        args={[36, 36, '#1e3a5f', '#13233e']}
        position={[0, 0.005, 0]}
      />

      {/* 2. Delineated Walkways (Safety Blue/Green Transit Paths) */}
      {/* Main Center Cross-Aisle */}
      <mesh position={[0, 0.008, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[width - 4, 1.8]} />
        <meshStandardMaterial color="#0c2340" roughness={0.6} />
      </mesh>
      {/* Walkway Border Stripes (Yellow Safety Lines) */}
      <mesh position={[0, 0.01, 0.9]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[width - 4, 0.08]} />
        <meshBasicMaterial color="#eab308" />
      </mesh>
      <mesh position={[0, 0.01, -0.9]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[width - 4, 0.08]} />
        <meshBasicMaterial color="#eab308" />
      </mesh>

      {/* North-South Transit Corridors */}
      {[-12, -4, 4, 12].map((xPos, idx) => (
        <group key={idx} position={[xPos, 0, 0]}>
          <mesh position={[0, 0.007, 0]} rotation={[-Math.PI / 2, 0, 0]}>
            <planeGeometry args={[1.4, depth - 4]} />
            <meshStandardMaterial color="#0c2340" roughness={0.6} />
          </mesh>
          <mesh position={[-0.7, 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]}>
            <planeGeometry args={[0.08, depth - 4]} />
            <meshBasicMaterial color="#eab308" />
          </mesh>
          <mesh position={[0.7, 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]}>
            <planeGeometry args={[0.08, depth - 4]} />
            <meshBasicMaterial color="#eab308" />
          </mesh>
        </group>
      ))}

      {/* 3. Utility Service Trench (with metal diamond grating) */}
      <mesh position={[0, 0.006, -9.5]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[width - 6, 0.7]} />
        <meshStandardMaterial color="#1e293b" metalness={0.8} roughness={0.4} />
      </mesh>
      <mesh position={[0, 0.006, 9.5]} rotation={[-Math.PI / 2, 0, 0]}>
        <planeGeometry args={[width - 6, 0.7]} />
        <meshStandardMaterial color="#1e293b" metalness={0.8} roughness={0.4} />
      </mesh>

      {/* 4. Low Industrial Perimeter Boundary Walls */}
      {/* Back Wall */}
      <mesh position={[0, 1.2, -depth / 2]} castShadow receiveShadow>
        <boxGeometry args={[width, 2.4, 0.4]} />
        <meshStandardMaterial color="#0f172a" roughness={0.8} metalness={0.2} />
      </mesh>
      {/* Back Wall Hazard Baseboard */}
      <mesh position={[0, 0.15, -depth / 2 + 0.21]}>
        <boxGeometry args={[width, 0.3, 0.02]} />
        <meshBasicMaterial color="#eab308" />
      </mesh>

      {/* Left Wall */}
      <mesh position={[-width / 2, 1.2, 0]} castShadow receiveShadow>
        <boxGeometry args={[0.4, 2.4, depth]} />
        <meshStandardMaterial color="#0f172a" roughness={0.8} metalness={0.2} />
      </mesh>
      {/* Right Wall */}
      <mesh position={[width / 2, 1.2, 0]} castShadow receiveShadow>
        <boxGeometry args={[0.4, 2.4, depth]} />
        <meshStandardMaterial color="#0f172a" roughness={0.8} metalness={0.2} />
      </mesh>

      {/* 5. Structural Steel I-Beam Columns (Pillars) */}
      {[
        [-width / 2 + 1, -depth / 2 + 1],
        [0, -depth / 2 + 1],
        [width / 2 - 1, -depth / 2 + 1],
        [-width / 2 + 1, depth / 2 - 1],
        [0, depth / 2 - 1],
        [width / 2 - 1, depth / 2 - 1],
      ].map(([x, z], i) => (
        <group key={i} position={[x, 0, z]}>
          {/* Main Vertical Column */}
          <mesh position={[0, 3.5, 0]} castShadow>
            <boxGeometry args={[0.55, 7.0, 0.55]} />
            <meshStandardMaterial color="#1e3a5f" metalness={0.8} roughness={0.3} />
          </mesh>
          {/* Concrete Footing Pedestal */}
          <mesh position={[0, 0.4, 0]} castShadow receiveShadow>
            <boxGeometry args={[0.9, 0.8, 0.9]} />
            <meshStandardMaterial color="#334155" roughness={0.6} />
          </mesh>
          {/* Hazard Warning Stripe on Pillar Base */}
          <mesh position={[0, 0.65, 0.46]}>
            <planeGeometry args={[0.8, 0.2]} />
            <meshBasicMaterial color="#eab308" />
          </mesh>
        </group>
      ))}

      {/* 6. Overhead Metal Roof Trusses */}
      {[-width / 3, 0, width / 3].map((x, i) => (
        <group key={i} position={[x, 6.8, 0]}>
          {/* Horizontal Cross Truss */}
          <mesh castShadow>
            <boxGeometry args={[0.3, 0.4, depth]} />
            <meshStandardMaterial color="#1e293b" metalness={0.8} roughness={0.3} />
          </mesh>
          {/* Suspended High-Bay LED Lamp Fixtures */}
          {[-8, 0, 8].map((z, j) => (
            <group key={j} position={[0, -0.4, z]}>
              <mesh position={[0, 0.15, 0]}>
                <cylinderGeometry args={[0.02, 0.02, 0.4, 8]} />
                <meshStandardMaterial color="#64748b" metalness={0.9} />
              </mesh>
              <mesh position={[0, -0.1, 0]}>
                <coneGeometry args={[0.5, 0.35, 16, 1, true]} rotation={[0, 0, 0]} />
                <meshStandardMaterial color="#334155" metalness={0.7} roughness={0.3} />
              </mesh>
              {/* Glowing LED emitter disk */}
              <mesh position={[0, -0.18, 0]}>
                <cylinderGeometry args={[0.35, 0.35, 0.04, 16]} />
                <meshStandardMaterial color="#e0f2fe" emissive="#38bdf8" emissiveIntensity={2.0} />
              </mesh>
            </group>
          ))}
        </group>
      ))}

      {/* 7. Elevated Inspection Catwalk / Mezzanine along Back Wall */}
      <group position={[0, 2.4, -depth / 2 + 1.6]}>
        {/* Catwalk Grated Deck */}
        <mesh position={[0, 0, 0]} castShadow receiveShadow>
          <boxGeometry args={[24, 0.12, 1.6]} />
          <meshStandardMaterial color="#1e293b" metalness={0.8} roughness={0.4} />
        </mesh>
        {/* Front Yellow Safety Railing */}
        <mesh position={[0, 0.55, 0.78]}>
          <boxGeometry args={[24, 0.06, 0.04]} />
          <meshStandardMaterial color="#eab308" metalness={0.4} roughness={0.4} />
        </mesh>
        <mesh position={[0, 1.05, 0.78]}>
          <boxGeometry args={[24, 0.06, 0.04]} />
          <meshStandardMaterial color="#eab308" metalness={0.4} roughness={0.4} />
        </mesh>
        {/* Vertical Railing Posts */}
        {Array.from({ length: 13 }).map((_, i) => (
          <mesh key={i} position={[-12 + i * 2, 0.55, 0.78]}>
            <cylinderGeometry args={[0.025, 0.025, 1.1, 8]} />
            <meshStandardMaterial color="#eab308" metalness={0.4} />
          </mesh>
        ))}
      </group>

      {/* 8. Raw Materials & Tooling Staging Area (Corner storage) */}
      <group position={[14, 0, -9]}>
        {/* Wooden / Plastic Storage Pallets */}
        <mesh position={[0, 0.08, 0]} castShadow receiveShadow>
          <boxGeometry args={[1.8, 0.16, 1.8]} />
          <meshStandardMaterial color="#475569" roughness={0.8} />
        </mesh>
        {/* Machined Metal Ingot Stacks */}
        <mesh position={[0, 0.45, 0]} castShadow>
          <boxGeometry args={[1.3, 0.6, 1.3]} />
          <meshStandardMaterial color="#94a3b8" metalness={0.9} roughness={0.2} />
        </mesh>
        {/* Coolant Drums / Oil Barrels */}
        {[-0.6, 0.6].map((x, i) => (
          <mesh key={i} position={[x, 0.55, 1.4]} castShadow>
            <cylinderGeometry args={[0.3, 0.3, 1.0, 16]} />
            <meshStandardMaterial color="#0284c7" metalness={0.7} roughness={0.3} />
          </mesh>
        ))}
      </group>
    </group>
  );
}

