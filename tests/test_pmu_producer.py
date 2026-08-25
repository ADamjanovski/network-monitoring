import unittest
from unittest.mock import patch

from pmu_producer import (
    PMUDataProducer,
    overcurrent_anomaly_magnitude,
    voltage_anomaly_variation,
)


class PMUProducerSeverityTest(unittest.TestCase):
    def setUp(self):
        self.producer = PMUDataProducer.__new__(PMUDataProducer)
        self.producer.system_frequency = 50.0
        self.pmu_config = {
            'pmu_id': 'test-pmu',
            'location': 'test-location',
            'substation': 'test-substation',
            'region': 'test-region',
            'base_voltage': 20000.0,
            'base_current': 400.0,
            'voltage_level': 'MV',
        }

    def test_generated_magnitudes_cover_all_severity_levels(self):
        expected_scores = (0.15, 0.40, 0.65, 0.90)

        for score in expected_scores:
            sag_voltage = 20000.0 * (1 + voltage_anomaly_variation(score, True))
            swell_voltage = 20000.0 * (1 + voltage_anomaly_variation(score, False))
            current = overcurrent_anomaly_magnitude(400.0, score)

            self.assertAlmostEqual(score, (18000.0 - sag_voltage) / 2000.0)
            self.assertAlmostEqual(score, (swell_voltage - 22000.0) / 2000.0)
            self.assertAlmostEqual(score, (current - 1200.0) / 800.0)

    @patch('pmu_producer.random.uniform')
    @patch('pmu_producer.random.random', side_effect=(0.0, 0.0, 1.0, 1.0))
    def test_voltage_anomaly_does_not_force_current_anomaly(self, _random, uniform):
        uniform.side_effect = self.normal_or_medium_severity
        measurement = self.producer.generate_measurement(self.pmu_config)

        self.assertLess(measurement['voltage_magnitude'], 18000.0)
        self.assertEqual(400.0, measurement['current_magnitude'])

    @patch('pmu_producer.random.uniform')
    @patch('pmu_producer.random.random', side_effect=(1.0, 0.0, 1.0))
    def test_current_anomaly_does_not_force_voltage_anomaly(self, _random, uniform):
        uniform.side_effect = self.normal_or_medium_severity
        measurement = self.producer.generate_measurement(self.pmu_config)

        self.assertEqual(20000.0, measurement['voltage_magnitude'])
        self.assertGreater(measurement['current_magnitude'], 1200.0)

    @staticmethod
    def normal_or_medium_severity(lower, upper):
        if (lower, upper) == (0.05, 1.0):
            return 0.5
        return 0.0


if __name__ == '__main__':
    unittest.main()
