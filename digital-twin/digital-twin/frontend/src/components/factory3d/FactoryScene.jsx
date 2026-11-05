import { useState, useMemo, Suspense } from 'react';
import { Canvas } from '@react-three/fiber';
import { RotateCcw, Box, Compass } from 'lucide-react';
import FactoryFloor from './FactoryFloor';
import Machine3D from './Machine3D';
import RoboticArm from './RoboticArm';
import ConveyorBelt from './ConveyorBelt';
import StorageTank from './StorageTank';
import FactoryCamera from './FactoryCamera';

// Standard 3D layout coordinates for the 12 machines across 3 manufacturing lines
const MACHINE_LAYOUT = {
  A: { pos: [-9, 0, -6], rot: [0, 0, 0] },
  D: { pos: [-3, 0, -6], rot: [0, 0, 0] },
  B: { pos: [3, 0, -6], rot: [0, 0, 0] },
  C: { pos: [9, 0, -6], rot: [0, 0, 0] },

  E: { pos: [-9, 0, 0], rot: [0, 0, 0] },
  F: { pos: [-3, 0, 0], rot: [0, 0, 0] },
  G: { pos: [3, 0, 0], rot: [0, 0, 0] },
  H: { pos: [9, 0, 0], rot: [0, 0, 0] },

  K: { pos: [-9, 0, 6], rot: [0, 0, 0] },
  I: { pos: [-3, 0, 6], rot: [0, 0, 0] },
  J: { pos: [3, 0, 6], rot: [0, 0, 0] },
  L: { pos: [9, 0, 6], rot: [0, 0, 0] },
};

export default function FactoryScene({ machines = [], selectedId, onSelect }) {
  const [isResetting, setIsResetting] = useState(false);

  // Status counts for overview banner
  const counts = useMemo(() => {
    let running = 0, warning = 0, offline = 0;
    machines.forEach((m) => {
      if (m.status === 'RUNNING') running++;
      else if (m.status === 'WARNING') warning++;
      else offline++;
    });
    return { running, warning, offline, total: machines.length };
  }, [machines]);

  // Find 3D position of selected machine for camera focus
  const selectedMachine = machines.find((m) => m.id === selectedId);
  const selectedPos = useMemo(() => {
    if (!selectedMachine) return null;
    const layout = MACHINE_LAYOUT[selectedMachine.code];
    return layout ? layout.pos : null;
  }, [selectedMachine]);

  return (
    <div
      style={{
        width: '100%',
        height: '430px',
        position: 'relative',
        background: '#060e1d',
        overflow: 'hidden',
        userSelect: 'none',
      }}
    >
      {/* 1. Header Overlay Toolbar */}
      <div
        style={{
          position: 'absolute',
          top: '12px',
          left: '16px',
          right: '16px',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          pointerEvents: 'none',
          zIndex: 20,
        }}
      >
        {/* Title & Live Status Counters */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            background: 'rgba(8, 20, 40, 0.9)',
            border: '1px solid rgba(30, 58, 95, 0.8)',
            backdropFilter: 'blur(10px)',
            borderRadius: '10px',
            padding: '6px 14px',
            pointerEvents: 'auto',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Box size={18} color="#38bdf8" />
            <span style={{ fontSize: '13px', fontWeight: 700, color: '#f8fafc', letterSpacing: '0.3px' }}>
              Factory Overview
            </span>
            <span style={{ fontSize: '11px', color: '#64748b', fontWeight: 600 }}>
              (Interactive 3D)
            </span>
          </div>

          <div style={{ width: '1px', height: '16px', background: '#1e3a5f' }} />

          {/* Quick Status Badges */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', fontSize: '12px', fontWeight: 600 }}>
            <span style={{ display: 'flex', alignItems: 'center', gap: '5px', color: '#22c55e' }}>
              <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#22c55e' }} />
              {counts.running} Normal
            </span>
            {counts.warning > 0 && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '5px', color: '#f59e0b' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#f59e0b' }} />
                {counts.warning} Warning
              </span>
            )}
            {counts.offline > 0 && (
              <span style={{ display: 'flex', alignItems: 'center', gap: '5px', color: '#ef4444' }}>
                <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#ef4444' }} />
                {counts.offline} Offline
              </span>
            )}
          </div>
        </div>

        {/* Action Controls: Reset Camera & Navigation Tip */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', pointerEvents: 'auto' }}>
          <button
            onClick={() => setIsResetting(true)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '6px',
              background: 'rgba(8, 20, 40, 0.9)',
              border: '1px solid rgba(30, 58, 95, 0.8)',
              color: '#93c5fd',
              borderRadius: '8px',
              padding: '6px 12px',
              fontSize: '12px',
              fontWeight: 600,
              cursor: 'pointer',
              transition: 'all 0.15s ease',
            }}
            onMouseEnter={(e) => {
              e.currentTarget.style.background = '#0f274a';
              e.currentTarget.style.color = '#ffffff';
            }}
            onMouseLeave={(e) => {
              e.currentTarget.style.background = 'rgba(8, 20, 40, 0.9)';
              e.currentTarget.style.color = '#93c5fd';
            }}
            title="Reset Camera to Isometric View"
          >
            <RotateCcw size={14} />
            Reset View
          </button>
        </div>
      </div>

      {/* 2. Interactive Navigation Hint (Bottom Center) */}
      <div
        style={{
          position: 'absolute',
          bottom: '10px',
          left: '50%',
          transform: 'translateX(-50%)',
          background: 'rgba(8, 20, 40, 0.8)',
          border: '1px solid rgba(30, 58, 95, 0.5)',
          borderRadius: '20px',
          padding: '4px 14px',
          fontSize: '11px',
          color: '#94a3b8',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          pointerEvents: 'none',
          zIndex: 20,
        }}
      >
        <Compass size={13} color="#38bdf8" />
        <span>Drag to rotate • Scroll to zoom • Click machine to inspect</span>
      </div>

      {/* 3. Three.js Canvas Scene */}
      <Canvas
        shadows
        camera={{ position: [22, 18, 22], fov: 42 }}
        gl={{ antialias: true, alpha: false, powerPreference: 'high-performance' }}
        onPointerMissed={() => {
          // If clicked empty space, retain current selection
        }}
      >
        <color attach="background" args={['#060e1d']} />
        <fog attach="fog" args={['#060e1d', 34, 75]} />

        {/* Ambient & Directional Lighting */}
        <ambientLight color="#0e1f38" intensity={1.3} />

        <directionalLight
          position={[18, 26, 16]}
          intensity={2.2}
          castShadow
          shadow-mapSize={[2048, 2048]}
          shadow-camera-near={0.5}
          shadow-camera-far={65}
          shadow-camera-left={-22}
          shadow-camera-right={22}
          shadow-camera-top={22}
          shadow-camera-bottom={-22}
          shadow-bias={-0.0004}
        />

        {/* Cool High-Bay Accent Spotlights */}
        <pointLight position={[-6, 7, -6]} color="#38bdf8" intensity={1.8} distance={20} decay={2} />
        <pointLight position={[6, 7, 0]} color="#38bdf8" intensity={1.8} distance={20} decay={2} />
        <pointLight position={[-6, 7, 6]} color="#38bdf8" intensity={1.8} distance={20} decay={2} />

        {/* Camera Controller */}
        <FactoryCamera
          selectedPosition={selectedPos}
          isResetting={isResetting}
          onResetComplete={() => setIsResetting(false)}
        />

        <Suspense fallback={null}>
          {/* Factory Floor & Infrastructure */}
          <FactoryFloor width={38} depth={28} />

          {/* Utility Storage Tanks */}
          <StorageTank position={[-15, 0, -6]} radius={1.1} height={3.8} color="#1e3a5f" label="COOLANT 1" />
          <StorageTank position={[-15, 0, 0]} radius={1.2} height={4.2} color="#0f766e" label="HYDRAULIC OIL" />
          <StorageTank position={[-15, 0, 6]} radius={1.0} height={3.5} color="#0369a1" label="DIE LUBE" />

          {/* Automated Conveyor Systems along the production lines */}
          <ConveyorBelt position={[0, 0, -3.8]} length={22} speed={1.1} itemCount={7} />
          <ConveyorBelt position={[0, 0, 2.2]} length={22} speed={1.3} itemCount={7} />
          <ConveyorBelt position={[0, 0, 8.2]} length={22} speed={1.0} itemCount={7} />

          {/* Articulated Industrial Robotic Arms */}
          <RoboticArm position={[-6, 0, -3.8]} scale={0.9} />
          <RoboticArm position={[6, 0, -3.8]} scale={0.9} />
          <RoboticArm position={[-6, 0, 2.2]} scale={0.9} />
          <RoboticArm position={[6, 0, 2.2]} scale={0.9} />
          <RoboticArm position={[0, 0, 8.2]} scale={0.9} />

          {/* 12 Interactive CNC / Casting Machines */}
          {machines.map((machine) => {
            const layout = MACHINE_LAYOUT[machine.code];
            if (!layout) return null;
            return (
              <Machine3D
                key={machine.id}
                machine={machine}
                position={layout.pos}
                rotation={layout.rot}
                isSelected={machine.id === selectedId}
                onSelect={onSelect}
              />
            );
          })}
        </Suspense>
      </Canvas>
    </div>
  );
}

