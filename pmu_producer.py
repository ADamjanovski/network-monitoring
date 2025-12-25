import json
import time
import random
import argparse
from datetime import datetime, timezone
from kafka import KafkaProducer


class PMUDataProducer:
    
    def __init__(self, kafka_bootstrap_servers: str , 
                 topic: str,
                 regions: list ,
                 substations_dict : dict,
                 pmus_per_substation: int):

        self.topic = topic
        self.producer = KafkaProducer(
            bootstrap_servers=kafka_bootstrap_servers,
            security_protocol="PLAINTEXT",
            api_version=(3, 5, 0)
        )
        if regions is None:
            regions = ['NORTH', 'SOUTH', 'EAST', 'WEST', 'CENTRAL']
        
        self.pmu_configs = []
        for region in regions:
            for substation_name in substations_dict[region]:
                for pmu_num in range(1, pmus_per_substation + 1):
                    pmu_id = f"{region}_{substation_name}_PMU{pmu_num:03d}"

                    location = substation_name

                    base_voltage = 20000.0
                    base_current = 100.0 
                    base_frequency = 50.0
                    
                    self.pmu_configs.append({
                        'pmu_id': pmu_id,
                        'location': location,
                        'base_voltage': base_voltage,
                        'base_current': base_current,
                        'base_frequency': base_frequency
                    })
    
    def generate_measurement(self, pmu_config):
        voltage_variation = random.uniform(-0.05, 0.05)  
        current_variation = random.uniform(0.0,100.0)
        frequency_variation = random.uniform(-0.01, 0.01)  
        
        anomaly_chance =random.random()
        if anomaly_chance < 0.05:
            voltage_variation = random.uniform(-0.10, 0.10)  
            voltage_variation = voltage_variation+0.051 if voltage_variation > 0 else voltage_variation-0.051
            current_variation = random.uniform(101, 200.0) 
            frequency_variation = random.uniform(-0.99, 0.99) 
            frequency_variation = frequency_variation+0.011 if frequency_variation > 0 else frequency_variation-0.011
        
        if anomaly_chance < 0.005:
            current_variation = random.uniform(250, 300.0) 


        voltage = pmu_config['base_voltage'] * (1 + voltage_variation)
        current = current_variation
        frequency = pmu_config['base_frequency'] + frequency_variation
        
        timestamp = int(datetime.now(timezone.utc).timestamp()*1000)
        
        measurement = {
            'timestamp': timestamp,
            'key': pmu_config['pmu_id'],
            'location': pmu_config['location'],
            'voltage': round(voltage, 2),
            'current': round(current, 2),
            'frequency': round(frequency, 4)
        }
        
        return measurement
    
    def produce_measurements(self):


        while True:
            pmu=random.choice(self.pmu_configs)
            measurement = self.generate_measurement(pmu)
            self.producer.send(
                topic=self.topic,
                value=json.dumps(measurement).encode("utf-8")
            )
            time.sleep(random.randint(500, 2000) / 1000.0)


def main():
    
    args = parser.parse_args()
    
    producer = PMUDataProducer(
        kafka_bootstrap_servers=args.bootstrap_servers,
        topic=args.topic,
        regions=args.regions,
        substations_per_region=args.substations_per_region,
        pmus_per_substation=args.pmus_per_substation
    )
    
    producer.produce_measurements()


if __name__ == '__main__':

    north_macedonia_regions = {
        "Skopje": ["Skopje"],
        "Polog": ["Tetovo", "Gostivar", "Debar", "Kicevo"],
        "Southwestern": ["Ohrid", "Struga", "Kicevo", "Resen"],
        "Pelagonia": ["Bitola", "Prilep", "Demir_Hisar", "Krusevo"],
        "Vardar": ["Veles", "Kavadarci", "Negotino"],
        "Southeastern": ["Strumica", "Gevgelija", "Valandovo", "Radovis"],
        "Eastern": ["Stip", "Kocani", "Vinica", "Berovo", "Delcevo"],
        "Northeastern": ["Kumanovo", "Kriva_Palanka", "Kratovo"],
    }
    producer = PMUDataProducer(
        kafka_bootstrap_servers='localhost:9092',
        topic='pmu-measurements',
        regions=north_macedonia_regions.keys(),
        substations_dict=north_macedonia_regions,
        pmus_per_substation=2
    )
    
    producer.produce_measurements()

