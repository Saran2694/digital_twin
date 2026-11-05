import { useRef, useEffect } from 'react';
import { useThree, useFrame } from '@react-three/fiber';
import { OrbitControls } from '@react-three/drei';
import * as THREE from 'three';

export default function FactoryCamera({ selectedPosition, isResetting, onResetComplete }) {
  const controlsRef = useRef();
  const { camera } = useThree();

  // Target vectors for smooth interpolation
  const desiredTarget = useRef(new THREE.Vector3(0, 1, 0));
  const desiredPosition = useRef(new THREE.Vector3(22, 18, 22));

  // Default overview parameters
  const DEFAULT_TARGET = new THREE.Vector3(0, 1, 0);
  const DEFAULT_POSITION = new THREE.Vector3(22, 18, 22);

  useEffect(() => {
    if (selectedPosition) {
      // Focus on the selected machine with an elevated inspection view
      desiredTarget.current.set(selectedPosition[0], 1.2, selectedPosition[2]);
      desiredPosition.current.set(
        selectedPosition[0] + 9,
        selectedPosition[1] + 8,
        selectedPosition[2] + 9
      );
    } else {
      desiredTarget.current.copy(DEFAULT_TARGET);
      desiredPosition.current.copy(DEFAULT_POSITION);
    }
  }, [selectedPosition]);

  useEffect(() => {
    if (isResetting) {
      desiredTarget.current.copy(DEFAULT_TARGET);
      desiredPosition.current.copy(DEFAULT_POSITION);
      onResetComplete?.();
    }
  }, [isResetting, onResetComplete]);

  useFrame(() => {
    if (!controlsRef.current) return;

    // Smoothly interpolate camera position and orbit target
    camera.position.lerp(desiredPosition.current, 0.05);
    controlsRef.current.target.lerp(desiredTarget.current, 0.05);
    controlsRef.current.update();
  });

  return (
    <OrbitControls
      ref={controlsRef}
      makeDefault
      enableDamping
      dampingFactor={0.06}
      minDistance={7}
      maxDistance={46}
      maxPolarAngle={Math.PI / 2 - 0.06} // Disallow view below ground floor
      minPolarAngle={0.1}
    />
  );
}

