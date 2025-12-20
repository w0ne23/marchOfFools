package marchoffools.server.game;

import java.util.Random;

import marchoffools.common.model.GameMode;
import marchoffools.server.game.spawn.ObstacleSpawnStrategy;
import marchoffools.server.game.spawn.MonsterSpawnStrategy;
import marchoffools.server.game.spawn.SpawnConfig;

public class ObstacleSpawner {
	
    private ObstacleSpawnStrategy obstacleStrategy;
    private MonsterSpawnStrategy monsterStrategy;
    
    private static final long MIN_SPAWN_GAP = 100;
    private Random random = new Random();
    
    private long lastSpawnTime = 0;
    
    public ObstacleSpawner() {
        this.obstacleStrategy = new ObstacleSpawnStrategy(1000, 1500, 1, 1);
        this.monsterStrategy = new MonsterSpawnStrategy(1000, 1500, 1, 1);
    }
    
    public void updateStrategies(GameMode gameMode) {
        SpawnConfig config = gameMode.getSpawnConfig();
        
        this.obstacleStrategy = new ObstacleSpawnStrategy(
            config.getObstacleMinInterval(),
            config.getObstacleMaxInterval(),
            config.getGroundWeight(),
            config.getAirWeight()
        );
        
        this.monsterStrategy = new MonsterSpawnStrategy(
            config.getMonsterMinInterval(),
            config.getMonsterMaxInterval(),
            config.getSoftWeight(),
            config.getHardWeight()
        );
        
        System.out.println("[Spawner] Strategies updated: " + config);
    }
    
    public void startSpawn() {
        long delay = 800;
        boolean r = random.nextBoolean();
        
        obstacleStrategy.startSpawn(r ? 0 : delay);
        monsterStrategy.startSpawn(r ? delay : 0);
        
        System.out.println("[Spawner] Spawn started - " + (r ? "obs" : "mon") + " first");
    }
    
    public void trySpawn(TrackController track) {
    	if (true) return;
        long now = System.currentTimeMillis();
        
        // 마지막 스폰 후 최소 시간 경과 확인
        if (now - lastSpawnTime < MIN_SPAWN_GAP) {
            return;
        }
        
        boolean spawned = false;
        
        if (obstacleStrategy.shouldSpawn(now)) {
            track.addObstacle(obstacleStrategy.createObstacle());
            spawned = true;
        } else if (monsterStrategy.shouldSpawn(now)) {
            track.addObstacle(monsterStrategy.createObstacle());
            spawned = true;
        }
        
        if (spawned) {
            lastSpawnTime = now;
        }
    }
    
    public void reset() {
        obstacleStrategy.reset();
        monsterStrategy.reset();
        lastSpawnTime = 0;
        System.out.println("[Spawner] Reset complete");
    }
}