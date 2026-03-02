import json
import time
import random
import math
from datetime import datetime, timezone
from kafka import KafkaProducer


class PMUDataProducer:
    
    def __init__(self, kafka_bootstrap_servers: str , 
                 topic: str,
                 regions: list ,
                 substations_dict : dict):

        self.topic = topic
        self.producer = KafkaProducer(
            bootstrap_servers=kafka_bootstrap_servers,
            security_protocol="PLAINTEXT",
            api_version=(3, 5, 0)
        )
        self.system_frequency = 50.0
        
        self.pmu_configs = []
        pmu_counter = 1
        for region in regions:
            for substation_name in substations_dict[region]:

                pmus_in_substation = random.randint(4, 6)
                
                for pmu_idx in range(pmus_in_substation):
                    
                    pmu_id = f"{region}_{substation_name}_PMU{(pmu_idx+1):03d}"

                    self.pmu_configs.append({
                        'pmu_id': pmu_id,
                        'region': region,
                        'substation': f"{region}_{substation_name}",
                        'location': f"{region} Region, {substation_name}, Unit {pmu_idx+1}",
                        'voltage_level': 'MV',
                        'base_voltage': 20000.0,
                        'base_current': 400.0,
                        'base_frequency': 50.0
                    })

                    pmu_counter += 1

        print(f"Generated {len(self.pmu_configs)} PMU's")
    
    def generate_measurement(self, pmu_config):
        voltage_variation = random.uniform(-0.02, 0.02)  
        current_variation = random.uniform(-50,50)
        frequency_variation = random.uniform(-0.005, 0.005)  
        
        anomaly_chance =random.random()

        if anomaly_chance < 0.005:
            if random.random() < 0.5:
                voltage_variation = random.uniform(-0.15, -0.11) ## VOLTAGE SAG
            else:
                voltage_variation = random.uniform(0.11, 0.15) ## VOLTAGE SWELL
            
        if anomaly_chance < 0.005: 
            current_variation = random.uniform(400, 900)
            
        if anomaly_chance < 0.01: 
            if random.random() < 0.5:
                frequency_variation = random.uniform(-0.25, -0.21)
            else:
                frequency_variation = random.uniform(0.21, 0.25)
            


        voltage = pmu_config['base_voltage'] * (1 + voltage_variation)
        current = pmu_config['base_current'] + current_variation
        frequency = self.system_frequency + frequency_variation
        
        timestamp = int(datetime.now(timezone.utc).timestamp()*1000)
        
        measurement = {
            'timestamp': timestamp,
            'pmu_id': pmu_config['pmu_id'],
            'location': pmu_config['location'],
            'substation': pmu_config['substation'],
            'region': pmu_config['region'],
            'voltage_magnitude': round(voltage, 2),
            'current_magnitude': round(current, 2),
            'frequency': round(frequency, 6),
            'voltage_level': pmu_config['voltage_level']
        }
        
        return measurement
    
    def produce_measurements(self):
        print("Starting to send measurements to Kafka")
        measurement_count = 0
        last_frequency_update = time.time()

        # --- FREQUENCY STATE ---
        last_event_check = last_frequency_update
        active_event_remaining = 0.0   # seconds
        active_event_rate = 0.0        # Hz/s
        event_cooldown_until = last_frequency_update  # prevents too frequent events
        # --- END FREQUENCY STATE ---

        while True:
            batch_timestamp = int(datetime.now(timezone.utc).timestamp() * 1000)

            for i, pmu in enumerate(self.pmu_configs):
                measurement = self.generate_measurement(pmu)
                measurement["timestamp"] = batch_timestamp + i
                self.producer.send(
                    topic=self.topic,
                    value=json.dumps(measurement).encode("utf-8")
                )
                measurement_count += 1

            self.producer.flush()
            current_time = time.time()

            # --- FREQUENCY UPDATE ---
            dt = max(0.001, current_time - last_frequency_update)
            last_frequency_update = current_time

            # Apply ongoing ramp event (creates RoCoF across the window)
            if active_event_remaining > 0.0:
                apply_dt = min(dt, active_event_remaining)
                self.system_frequency += active_event_rate * apply_dt
                active_event_remaining -= apply_dt
                if active_event_remaining <= 0.0:
                    active_event_rate = 0.0

            # Start new events occasionally (checked every ~1.5s)
            if current_time - last_event_check >= 1.5:

                # Only start if no event active + cooldown passed
                if (active_event_remaining <= 0.0 and
                        current_time >= event_cooldown_until and
                        random.random() < 0.03):  # lower => fewer deviations

                    event_selector = random.random()

                    if event_selector < 0.70:
                        # Normal dip: small -> mainly FREQUENCY_DEVIATION
                        delta = -random.uniform(0.10, 0.60)      # net Δf < 0.6
                        duration = random.uniform(3.0, 6.0)
                        print(f"EVENT: Normal Dip → {self.system_frequency:.3f} Hz")

                    elif event_selector < 0.95:
                        # Significant shift: enough net Δf to exceed warning RoCoF sometimes
                        direction = 1 if random.random() > 0.5 else -1
                        delta = direction * random.uniform(1.6, 2.6)  # net Δf >= ~1.5
                        duration = random.uniform(3.5, 6.0)
                        print(f"EVENT: Significant Shift → {self.system_frequency:.3f} Hz")

                    else:
                        # Critical collapse: aim for ~3 Hz net change over ~5s => RoCoF ~0.6 in 5s regression
                        # If you're already low, clamp will limit it (still realistic).
                        delta = -random.uniform(2.7, 3.4)
                        duration = random.uniform(4.0, 6.0)
                        print(f"!!! CRITICAL: Frequency Collapse → {self.system_frequency:.3f} Hz")

                    active_event_rate = delta / duration
                    active_event_remaining = duration

                    # Cooldown so events aren't back-to-back
                    event_cooldown_until = current_time + random.uniform(12.0, 25.0)

                last_event_check = current_time

            # Recovery to 50 Hz (scaled by dt/1.5)
            if abs(self.system_frequency - 50.0) > 0.001:
                deviation = abs(self.system_frequency - 50.0)
                if deviation > 1.0:
                    recovery_rate = 0.12
                elif deviation > 0.3:
                    recovery_rate = 0.08
                else:
                    recovery_rate = 0.04

                # During an active ramp, reduce recovery so it doesn't cancel RoCoF
                if active_event_remaining > 0.0:
                    recovery_rate *= 0.2

                self.system_frequency += (50.0 - self.system_frequency) * recovery_rate * (dt / 1.5)

            # Clamp to realistic range (≥47 Hz)
            self.system_frequency = max(self.system_frequency, 47.0)

            # --- END FREQUENCY UPDATE ---

            time.sleep(random.randint(500, 1200) / 1000.0)


if __name__ == '__main__':

    north_macedonia_regions = {
        "Skopje": ["Skopje", "Arachinovo"],
        "Polog": ["Tetovo", "Gostivar"],
        "Southwestern": ["Ohrid", "Struga"],
        "Pelagonia": ["Bitola", "Prilep"],
        "Vardar": ["Veles", "Kavadarci"],
        "Southeastern": ["Strumica", "Gevgelija"],
        "Eastern": ["Stip", "Kocani"],
        "Northeastern": ["Kumanovo", "Kriva Palanka"],
    }
    producer = PMUDataProducer(
        kafka_bootstrap_servers='localhost:9092',
        topic='pmu-measurements',
        regions=north_macedonia_regions.keys(),
        substations_dict=north_macedonia_regions,
    )
    
    producer.produce_measurements()

