import json
import time
import random
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

                pmus_in_substation = random.randint(2, 4)
                
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
        frequency_variation = random.uniform(-0.002, 0.002)  
        
        anomaly_chance =random.random()

        if anomaly_chance < 0.02:
            if random.random() < 0.5:
                voltage_variation = random.uniform(-0.15, -0.11) ## VOLTAGE SAG
            else:
                voltage_variation = random.uniform(0.11, 0.15) ## VOLTAGE SWELL
            
            current_variation = random.uniform(800, 1000) 
            
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

        while True:
            now_ms = int(datetime.now(timezone.utc).timestamp() * 1000)

            for pmu in self.pmu_configs:
                measurement = self.generate_measurement(pmu)
                measurement["timestamp"] = now_ms 

                self.producer.send(
                    topic=self.topic,
                    value=json.dumps(measurement).encode("utf-8")
                )
                print(f"Sent measurement to Kafka: {measurement}")
                measurement_count += 1

            
            self.producer.flush()
            print("\n\n\n\n")
            current_time = time.time()
            if current_time - last_frequency_update >= 1.0:

                if random.random() < 0.01:
                    if random.random() < 0.5:
                        self.system_frequency -= random.uniform(0.1, 0.3)
                        print(f"EVENT: Frequency DROP {self.system_frequency:.3f} Hz")

                        if random.random() < 0.3:
                            self.system_frequency -= random.uniform(0.02, 0.08)
                            print(f"AFTERSHOCK: extra DROP {self.system_frequency:.3f} Hz")
                    else:
                        self.system_frequency += random.uniform(0.1, 0.25)
                        print(f"EVENT: Frequency RAISE {self.system_frequency:.3f} Hz")

                if abs(self.system_frequency - 50.0) > 0.001:
                    self.system_frequency += (50.0 - self.system_frequency) * 0.05

                    if self.system_frequency < 50.0 and random.random() < 0.15:
                        self.system_frequency -= random.uniform(0.02, 0.08)
                        print(f"AFTERSHOCK: extra DROP {self.system_frequency:.3f} Hz")

                self.system_frequency = max(self.system_frequency, 49.0)
                last_frequency_update = current_time

                if measurement_count % 500 == 0:
                    print(f"SENT: {measurement_count} | FREQUENCY: {self.system_frequency:.4f} Hz")

            time.sleep(random.randint(500, 1500) / 1000.0)



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

