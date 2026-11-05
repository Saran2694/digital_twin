import { useRef } from 'react';
import { useFrame } from '@react-three/fiber';

export default function RoboticArm({ position = [0, 0, 0], rotation = [0, 0, 0], scale = 1 }) {
  const baseTurntableRef = useRef();
  const shoulderRef = useRef();
  const elbowRef = useRef();
  const wristRef = useRef();
  const toolRef = useRef();
  const sparkRef = useRef();

  useFrame(({ clock }) => {
    const t = clock.getElapsedTime();

    // Joint 1: Turntable Yaw
    if (baseTurntableRef.current) {
      baseTurntableRef.current.rotation.y = Math.sin(t * 0.9) * 0.85;
    }

    // Joint 2: Shoulder Pitch
    if (shoulderRef.current) {
      shoulderRef.current.rotation.z = -0.3 + Math.sin(t * 1.3) * 0.28;
    }

    // Joint 3: Elbow Pitch
    if (elbowRef.current) {
      elbowRef.current.rotation.z = 0.6 + Math.cos(t * 1.3) * 0.35;
    }

    // Joint 4: Wrist articulation
    if (wristRef.current) {
      wristRef.current.rotation.y = Math.sin(t * 2.1) * 0.5;
    }

    // Welding spark / indicator light pulse
    if (sparkRef.current) {
      sparkRef.current.intensity = 1.0 + Math.sin(t * 12) * 0.8;
    }
  });

  const robotColor = '#f97316'; // Industrial safety orange (Kuka / Fanuc)
  const jointColor = '#1e293b'; // Slate graphite
  const metalColor = '#64748b'; // Machined steel

  return (
    <group position={position} rotation={rotation} scale={scale}>
      {/* 1. Base Pedestal bolted to floor */}
      <mesh position={[0, 0.12, 0]} castShadow receiveShadow>
        <cylinderGeometry args={[0.55, 0.65, 0.24, 24]} />
        <meshStandardMaterial color={jointColor} metalness={0.8} roughness={0.3} />
      </mesh>

      {/* Yellow Safety Zone Ring on Floor */}
      <mesh position={[0, 0.01, 0]} rotation={[-Math.PI / 2, 0, 0]}>
        <ringGeometry args={[0.7, 0.85, 32]} />
        <meshBasicMaterial color="#eab308" />
      </mesh>

      {/* 2. Rotating Turntable (Joint 1) */}
      <group ref={baseTurntableRef} position={[0, 0.24, 0]}>
        <mesh position={[0, 0.18, 0]} castShadow>
          <cylinderGeometry args={[0.42, 0.48, 0.36, 24]} />
          <meshStandardMaterial color={robotColor} metalness={0.6} roughness={0.3} />
        </mesh>

        {/* Shoulder Fork Brackets */}
        <mesh position={[0, 0.45, 0]} castShadow>
          <boxGeometry args={[0.45, 0.35, 0.55]} />
          <meshStandardMaterial color={jointColor} metalness={0.7} roughness={0.3} />
        </mesh>

        {/* 3. Lower Arm / Shoulder (Joint 2) */}
        <group ref={shoulderRef} position={[0, 0.52, 0]}>
          {/* Shoulder Pivot Axis */}
          <mesh rotation={[Math.PI / 2, 0, 0]} castShadow>
            <cylinderGeometry args={[0.18, 0.18, 0.58, 20]} />
            <meshStandardMaterial color={metalColor} metalness={0.9} roughness={0.2} />
          </mesh>

          {/* Lower Arm Boom */}
          <mesh position={[0, 0.7, 0]} castShadow>
            <boxGeometry args={[0.22, 1.25, 0.28]} />
            <meshStandardMaterial color={robotColor} metalness={0.6} roughness={0.3} />
          </mesh>

          {/* Counterbalance Cylinder */}
          <mesh position={[-0.2, 0.45, 0]} rotation={[0, 0, -0.3]} castShadow>
            <cylinderGeometry args={[0.07, 0.07, 0.7, 16]} />
            <meshStandardMaterial color={metalColor} metalness={0.9} roughness={0.2} />
          </mesh>

          {/* 4. Upper Arm / Elbow (Joint 3) */}
          <group ref={elbowRef} position={[0, 1.35, 0]}>
            {/* Elbow Pivot Axis */}
            <mesh rotation={[Math.PI / 2, 0, 0]} castShadow>
              <cylinderGeometry args={[0.15, 0.15, 0.46, 20]} />
              <meshStandardMaterial color={jointColor} metalness={0.8} roughness={0.3} />
            </mesh>

            {/* Upper Arm Forearm */}
            <mesh position={[0, 0.65, 0]} castShadow>
              <cylinderGeometry args={[0.12, 0.16, 1.15, 16]} />
              <meshStandardMaterial color={robotColor} metalness={0.6} roughness={0.3} />
            </mesh>

            {/* 5. Articulated Wrist (Joint 4 & 5) */}
            <group ref={wristRef} position={[0, 1.25, 0]}>
              <mesh position={[0, 0.1, 0]} castShadow>
                <cylinderGeometry args={[0.12, 0.12, 0.2, 16]} />
                <meshStandardMaterial color={jointColor} metalness={0.8} roughness={0.3} />
              </mesh>

              {/* End Effector Tool Head */}
              <group ref={toolRef} position={[0, 0.25, 0]}>
                <mesh castShadow>
                  <boxGeometry args={[0.22, 0.12, 0.18]} />
                  <meshStandardMaterial color={metalColor} metalness={0.9} roughness={0.2} />
                </mesh>

                {/* Gripper Fingers */}
                <mesh position={[-0.08, 0.12, 0]} castShadow>
                  <boxGeometry args={[0.04, 0.18, 0.08]} />
                  <meshStandardMaterial color={jointColor} metalness={0.8} roughness={0.3} />
                </mesh>
                <mesh position={[0.08, 0.12, 0]} castShadow>
                  <boxGeometry args={[0.04, 0.18, 0.08]} />
                  <meshStandardMaterial color={jointColor} metalness={0.8} roughness={0.3} />
                </mesh>

                {/* Held Machined Metal Component */}
                <mesh position={[0, 0.15, 0]} castShadow>
                  <cylinderGeometry args={[0.065, 0.065, 0.14, 16]} />
                  <meshStandardMaterial color="#cbd5e1" metalness={0.95} roughness={0.15} />
                </mesh>

                {/* Tool status laser / spark point light */}
                <pointLight ref={sparkRef} position={[0, 0.25, 0]} color="#38bdf8" intensity={1.5} distance={2.5} decay={2} />
              </group>
            </group>
          </group>
        </group>
      </group>
    </group>
  );
}

