// ============================================================================
// PAGE BACK-END : CLUSTERING DES DISCUSSIONS (IA)
// Analyse de groupes thématiques
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/api_service.dart';
import 'widgets/admin_widgets.dart';

class ClusteringPage extends StatefulWidget {
  const ClusteringPage({Key? key}) : super(key: key);

  @override
  _ClusteringPageState createState() => _ClusteringPageState();
}

class _ClusteringPageState extends State<ClusteringPage> {
  late ApiService api;

  String selectedMethod = 'K-Means';
  int clusterCount = 6;
  bool isRunning = false;
  List<Cluster> clusters = [];

  @override
  void initState() {
    super.initState();
    api = ApiService();
  }

  Future<void> _runClustering() async {
    setState(() => isRunning = true);

    try {
      final results = await api.runClustering(
        method: selectedMethod.toLowerCase(),
        clusterCount: clusterCount,
      );

      setState(() {
        clusters = results;
        isRunning = false;
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("Clustering terminé avec succès"),
            backgroundColor: Colors.green,
          ),
        );
      }
    } catch (e) {
      setState(() => isRunning = false);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text("Erreur: ${e.toString()}"),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      body: Row(
        children: [
          AdminSidebar(
            activeIndex: 0,
            onItemTapped: (index) => _handleNavigation(index),
          ),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                _buildHeader(),
                Expanded(
                  child: SingleChildScrollView(
                    padding: const EdgeInsets.all(24),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        const Text(
                          "Clustering des Discussions",
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 24),

                        // Paramètres
                        _buildParametersSection(),

                        const SizedBox(height: 32),

                        // Métriques clustering
                        _buildMetricsSection(),

                        const SizedBox(height: 32),

                        // Clusters cards
                        const Text(
                          "Clusters Identifiés",
                          style: TextStyle(
                              fontSize: 18, fontWeight: FontWeight.bold),
                        ),
                        const SizedBox(height: 16),
                        _buildClustersGrid(),

                        const SizedBox(height: 32),

                        // Insights
                        _buildInsightsSection(),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ==================== WIDGETS UI ====================

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      color: Colors.white,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: const [
              Icon(Icons.admin_panel_settings, color: Colors.red, size: 32),
              SizedBox(width: 10),
              Text(
                "NAJA7NI — Admin",
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
            ],
          ),
          Row(
            children: [
              TextButton(
                onPressed: () {},
                child: const Text("Dashboard",
                    style: TextStyle(color: Colors.black87)),
              ),
              const SizedBox(width: 8),
              ElevatedButton.icon(
                onPressed: () async => await AuthService().logout(),
                icon: const Icon(Icons.logout, size: 18),
                label: const Text("Logout"),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.red,
                  foregroundColor: Colors.white,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildParametersSection() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            "Paramètres du Clustering",
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          Row(
            children: [
              Expanded(
                child: _buildDropdown(
                  label: "Méthode",
                  value: selectedMethod,
                  items: ['K-Means', 'DBSCAN', 'Hierarchical'],
                  onChanged: (val) {
                    if (val != null) setState(() => selectedMethod = val);
                  },
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildNumberPicker(
                  label: "Nombre de clusters",
                  value: clusterCount,
                  min: 2,
                  max: 12,
                  onChanged: (val) => setState(() => clusterCount = val),
                ),
              ),
              const Spacer(),
              ElevatedButton.icon(
                onPressed: isRunning ? null : _runClustering,
                icon: isRunning
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Icon(Icons.play_arrow),
                label: Text(isRunning ? "Exécution..." : "Lancer le Clustering"),
                style: ElevatedButton.styleFrom(
                  backgroundColor: Colors.purple,
                  foregroundColor: Colors.white,
                  padding:
                      const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildDropdown({
    required String label,
    required String value,
    required List<String> items,
    required ValueChanged<String?> onChanged,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500)),
        const SizedBox(height: 6),
        DropdownButtonFormField<String>(
          value: value,
          decoration: const InputDecoration(
            border: OutlineInputBorder(),
            contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          ),
          items: items
              .map((m) => DropdownMenuItem(value: m, child: Text(m)))
              .toList(),
          onChanged: onChanged,
        ),
      ],
    );
  }

  Widget _buildNumberPicker({
    required String label,
    required int value,
    required int min,
    required int max,
    required ValueChanged<int> onChanged,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(label, style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w500)),
        const SizedBox(height: 6),
        Container(
          decoration: BoxDecoration(
            border: Border.all(color: Colors.grey),
            borderRadius: BorderRadius.circular(4),
          ),
          child: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              IconButton(
                icon: const Icon(Icons.remove, size: 16),
                onPressed: value > min ? () => onChanged(value - 1) : null,
                visualDensity: VisualDensity.compact,
              ),
              Container(
                width: 50,
                alignment: Alignment.center,
                child: Text(
                  value.toString(),
                  style: const TextStyle(fontWeight: FontWeight.bold),
                ),
              ),
              IconButton(
                icon: const Icon(Icons.add, size: 16),
                onPressed: value < max ? () => onChanged(value + 1) : null,
                visualDensity: VisualDensity.compact,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildMetricsSection() {
    if (clusters.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(12),
        ),
        child: const Center(
          child: Text(
            "Lancez le clustering pour voir les métriques",
            style: TextStyle(color: Colors.grey),
          ),
        ),
      );
    }

    final totalForums = clusters.fold<int>(0, (sum, c) => sum + c.forumCount);
    final avgMessagesPerTopic = clusters.fold<double>(
            0, (sum, c) => sum + (c.forumCount / (c.themes.length > 0 ? c.themes.length : 1))) /
        clusters.length;
    final totalTopics = clusters.fold<int>(0, (sum, c) => sum + c.themes.length);

    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            "Métriques du Clustering",
            style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: _buildMetricItem("${totalForums} Forums", "Total"),
              ),
              Expanded(
                child: _buildMetricItem(
                    "${(avgMessagesPerTopic).toStringAsFixed(1)} Messages/Topic",
                    "Moyenne"),
              ),
              Expanded(
                child:
                    _buildMetricItem("$totalTopics Topics", "Sujets traités"),
              ),
              Expanded(
                child: _buildMetricItem("$clusterCount Clusters Actifs",
                    clusters.length.toString()),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildMetricItem(String value, String label) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.bold,
            color: Colors.purple,
          ),
        ),
        Text(
          label,
          style: TextStyle(
            fontSize: 12,
            color: Colors.grey[600],
          ),
        ),
      ],
    );
  }

  Widget _buildClustersGrid() {
    if (clusters.isEmpty) {
      return const Center(
        child: Text(
          "Aucun cluster généré. Lancez l'algorithme.",
          style: TextStyle(color: Colors.grey),
        ),
      );
    }

    return GridView.builder(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 3,
        crossAxisSpacing: 16,
        mainAxisSpacing: 16,
        childAspectRatio: 0.9,
      ),
      itemCount: clusters.length,
      itemBuilder: (context, index) => ClusterCard(cluster: clusters[index]),
    );
  }

  Widget _buildInsightsSection() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.purple.withOpacity(0.1), Colors.blue.withOpacity(0.1)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.purple.withOpacity(0.2)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(Icons.lightbulb, color: Colors.purple[700], size: 24),
              const SizedBox(width: 8),
              const Text(
                "Insights & Recommandations",
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                  color: Colors.black87,
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          ...List.generate(3, (index) {
            final insights = [
              "Cluster 'Java Spring' : Forte activité, suggérer un forum dédié aux meilleures pratiques.",
              "Cluster 'Mathématiques' : Participation faible, proposer des ressources supplémentaires.",
              "Cluster 'Projets' : Croissance rapide, envisager d'augmenter le modération.",
            ];
            return Padding(
              padding: const EdgeInsets.only(bottom: 12),
              child: Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    margin: const EdgeInsets.only(top: 4),
                    width: 8,
                    height: 8,
                    decoration: BoxDecoration(
                      color: Colors.purple,
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      insights[index],
                      style: const TextStyle(
                          fontSize: 13, height: 1.5, color: Colors.black87),
                    ),
                  ),
                ],
              ),
            );
          }),
        ],
      ),
    );
  }

  void _handleNavigation(int index) {
    // TODO: Implémenter navigation
  }
}
