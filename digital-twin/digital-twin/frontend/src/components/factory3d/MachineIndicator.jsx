import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';
import { Html } from '@react-three/drei';
import { STATUS, num } from '../../util';

export default function MachineIndicator({ machine, isSelected, isHovered, onSelect }) {
  const lightRef = useRef();
  const warningLightRef = useRef();

  const statusKey = machine?.status || 'OFFLINE';
  const st = STATUS[statusKey] || STATUS.OFFLINE;
  const isRunning = statusKey === 'RUNNING';
  const isWarning = statusKey === 'WARNING';
  const isOffline = statusKey === 'OFFLINE';

  useFrame(({ clock }) => {
    const t = clock.getElapsedTime();
    if (isWarning && warningLightRef.current) {
      // Warning pulses rapidly (yellow flash)
      const pulse = 0.5 + 0.5 * Math.sin(t * 6);
      warningLightRef.current.intensity = 1.0 + pulse * 2.0;
    } else if (isOffline && lightRef.current) {
      // Offline pulses slowly (red warning)
      const pulse = 0.4 + 0.6 * Math.sin(t * 3);
      lightRef.current.intensity = 0.8 + pulse * 1.5;
    }
  });

  return (
    <group position={[0, 0, 0]}>
      {/* Andon Signal Light Tower */}
      <group position={[1.05, 2.3, 0.95]}>
        {/* Mounting stalk */}
        <mesh position={[0, 0.4, 0]}>
          <cylinderGeometry args={[0.03, 0.03, 0.8, 12]} />
          <meshStandardMaterial color="#1e293b" metalness={0.8} roughness={0.3} />
        </mesh>

        {/* Tower Body / Pod */}
        <mesh position={[0, 0.85, 0]}>
          <cylinderGeometry args={[0.07, 0.07, 0.1, 16]} />
          <meshStandardMaterial color="#0f172a" metalness={0.9} roughness={0.2} />
        </mesh>

        {/* Red light (Top) */}
        <mesh position={[0, 1.15, 0]}>
          <cylinderGeometry args={[0.065, 0.065, 0.08, 16]} />
          <meshStandardMaterial
            color={isOffline ? '#ef4444' : '#450a0a'}
            emissive="#ef4444"
            emissiveIntensity={isOffline ? 3.0 : 0.1}
            roughness={0.2}
            toneMapped={false}
          />
        </mesh>

        {/* Amber / Yellow light (Middle) */}
        <mesh position={[0, 1.05, 0]}>
          <cylinderGeometry args={[0.065, 0.065, 0.08, 16]} />
          <meshStandardMaterial
            color={isWarning ? '#f59e0b' : '#451a03'}
            emissive="#f59e0b"
            emissiveIntensity={isWarning ? 3.0 : 0.1}
            roughness={0.2}
            toneMapped={false}
          />
        </mesh>

        {/* Green light (Bottom) */}
        <mesh position={[0, 0.95, 0]}>
          <cylinderGeometry args={[0.065, 0.065, 0.08, 16]} />
          <meshStandardMaterial
            color={isRunning ? '#22c55e' : '#052e16'}
            emissive="#22c55e"
            emissiveIntensity={isRunning ? 2.5 : 0.1}
            roughness={0.2}
            toneMapped={false}
          />
        </mesh>

        {/* Tower Cap */}
        <mesh position={[0, 1.21, 0]}>
          <cylinderGeometry args={[0.07, 0.07, 0.04, 16]} />
          <meshStandardMaterial color="#0f172a" metalness={0.9} roughness={0.2} />
        </mesh>

        {/* Localized Point Light glow */}
        {isRunning && (
          <pointLight position={[0, 0.95, 0]} color="#22c55e" intensity={1.5} distance={3.5} decay={2} />
        )}
        {isWarning && (
          <pointLight ref={warningLightRef} position={[0, 1.05, 0]} color="#f59e0b" intensity={2.0} distance={4.5} decay={2} />
        )}
        {isOffline && (
          <pointLight ref={lightRef} position={[0, 1.15, 0]} color="#ef4444" intensity={2.0} distance={4.0} decay={2} />
        )}
      </group>

      {/* Floating 2.5D Status Badge in 3D Space */}
      <Html
        position={[0, 3.6, 0]}
        center
        distanceFactor={22}
        zIndexRange={[100, 0]}
        style={{ pointerEvents: 'auto' }}
      >
        <div
          onClick={(e) => {
            e.stopPropagation();
            onSelect?.(machine.id);
          }}
          style={{
            cursor: 'pointer',
            userSelect: 'none',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            background: isSelected ? 'rgba(10, 25, 48, 0.96)' : 'rgba(7, 16, 32, 0.88)',
            backdropFilter: 'blur(8px)',
            border: `2px solid ${isSelected ? '#ffffff' : st.color}`,
            borderRadius: '20px',
            padding: '4px 10px 4px 6px',
            boxShadow: isSelected
              ? `0 0 20px ${st.color}, 0 4px 14px rgba(0,0,0,0.8)`
              : `0 0 10px ${st.color}44, 0 3px 10px rgba(0,0,0,0.6)`,
            transform: isHovered || isSelected ? 'scale(1.12)' : 'scale(1)',
            transition: 'transform 0.15s ease, box-shadow 0.15s ease, border-color 0.15s ease',
            whiteSpace: 'nowrap',
          }}
        >
          {/* Status Dot / Code Avatar */}
          <div
            style={{
              width: '22px',
              height: '22px',
              borderRadius: '50%',
              background: st.color,
              color: '#0a0f1d',
              fontWeight: 800,
              fontSize: '11px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: `0 0 8px ${st.color}`,
            }}
          >
            {machine.code}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', lineHeight: 1.15 }}>
            <span style={{ fontSize: '12px', fontWeight: 700, color: '#f8fafc' }}>
              {machine.name}
            </span>
            <span style={{ fontSize: '10px', fontWeight: 600, color: st.color }}>
              {isOffline ? 'OFFLINE' : `${num(machine.temperature, 0)}°C · ${st.label}`}
            </span>
          </div>
        </div>
      </Html>
    </group>
  );
}

