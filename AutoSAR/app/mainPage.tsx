import MapboxGL from '@rnmapbox/maps';
import { View, StyleSheet } from 'react-native';

MapboxGL.setAccessToken(process.env.MAPBOX_ACCESS_TOKEN);

export default function MainPage() {
  return (
    <View style={styles.page}>
      <MapboxGL.MapView style={styles.map}>
        <MapboxGL.Camera
          zoomLevel={14}
          centerCoordinate={[-1.93, 53.8]} // Example location
        />

        {/* Add IPP pin */}
        <MapboxGL.PointAnnotation id="ipp" coordinate={[-1.93, 53.8]} />

        {/* Add range rings and sectors here */}
      </MapboxGL.MapView>
    </View>
  );
}

const styles = StyleSheet.create({
  page: { flex: 1 },
  map: { flex: 1 }
});