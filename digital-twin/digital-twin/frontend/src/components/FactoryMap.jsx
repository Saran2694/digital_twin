import FactoryScene from './factory3d/FactoryScene';

export default function FactoryMap({ machines = [], selectedId, onSelect }) {
  return (
    <div className="factory-map">
      <FactoryScene
        machines={machines}
        selectedId={selectedId}
        onSelect={onSelect}
      />
    </div>
  );
}
