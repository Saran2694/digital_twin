import { useState, useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import CNCMachine from './CNCMachine';
import MachineIndicator from './MachineIndicator';
import { STATUS } from '../../util';

export default function Machine3D({
  machine,
  position = [0, 0, 0],
  rotation = [0, 0, 0],
  isSelected = false,
  onSelect,
}) {
  const [isHovered, setIsHovered] = useState(false);
  const selectionRingRef = useRef();

  const st = STATUS[machine?.status] || STATUS.OFFLINE;

  useFrame(({ clock }) => {
    if (isSelected && selectionRingRef.current) {
      const t = clock.getElapsedTime();
      // Pulsing animated selection ring on the floor
      const scale = 1.0 + Math.sin(t * 4) * 0.05;
      selectionRingRef.current.scale.set(scale, 1, scale);
    }
  });

  return (
    <group
      position={position}
      rotation={rotation}
      onPointerOver={(e) => {
        e.stopPropagation();
        setIsHovered(true);
        document.body.style.cursor = 'pointer';
      }}
      onPointerOut={(e) => {
        e.stopPropagation();
        setIsHovered(false);
        document.body.style.cursor = 'auto';
      }}
      onClick={(e) => {
        e.stopPropagation();
        if (machine?.id) {
          onSelect?.(machine.id);
        }
      }}
    >
      {/* 1. Industrial Yellow Hazard Safety Zone on the floor around this machine */}
      <group position={[0, 0.012, 0]}>
        {/* Safety Zone Boundary Outline */}
        <mesh rotation={[-Math.PI / 2, 0, 0]}>
          <planeGeometry args={[3.2, 3.0]} />
          <meshBasicMaterial color={isSelected ? '#38bdf8' : '#eab308'} wireframe />
        </mesh>

        {/* Hazard Zone Floor Plate */}
        <mesh rotation={[-Math.PI / 2, 0, 0]}>
          <planeGeometry args={[3.1, 2.9]} />
          <meshStandardMaterial
            color={isSelected ? '#0c2e59' : '#08172c'}
            roughness={0.6}
            metalness={0.3}
          />
        </mesh>
      </group>

      {/* 2. Active Selection Highlight Ring / Bracket on floor */}
      {isSelected && (
        <group ref={selectionRingRef} position={[0, 0.02, 0]}>
          <mesh rotation={[-Math.PI / 2, 0, 0]}>
            <ringGeometry args={[1.7, 1.9, 36]} />
            <meshBasicMaterial color={st.color} />
          </mesh>
          <pointLight position={[0, 0.3, 0]} color={st.color} intensity={2.5} distance={3.5} decay={2} />
        </group>
      )}

      {/* 3. The 3D CNC / Casting Machine Mesh */}
      <CNCMachine
        status={machine?.status || 'OFFLINE'}
        isHovered={isHovered}
        isSelected={isSelected}
      />

      {/* 4. Glowing Status Indicator Tower & Floating Tag */}
      <MachineIndicator
        machine={machine}
        isSelected={isSelected}
        isHovered={isHovered}
        onSelect={onSelect}
      />
    </group>
  );
}

