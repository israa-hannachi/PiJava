// ============================================================================
// PAGE BACK-END : STATISTIQUES FORUMS (ADMIN)
// Graphiques et métriques détaillées
// ============================================================================

import 'package:flutter/material.dart';
import '../services/api_service.dart';
import 'widgets/admin_widgets.dart';

class StatisticsPage extends StatefulWidget {
  const StatisticsPage({Key? key}) : super(key: key);

  @override
  _StatisticsPageState createState() => _StatisticsPageState();
}

class _StatisticsPageState extends State<StatisticsPage> {
  late ApiService api;
  ForumStats? stats;
  List<Map<String, dynamic>> topForums = [];
  List<Map<String, dynamic>> forumsByCategory = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    api = ApiService();
    _loadData();
  }

  Future<void> _loadData() async {
    setState(() => isLoading = true);
    try {
      final [statsData, topForumsData, byCategoryData] = await Future.wait([
        api.getForumStats(),
        api.getTopForumsByMessages(limit: 10),
        api.getForumsByCategory(),
      ]);

      setState(() {
        stats = statsData;
        topForums = topForumsData;
        forumsByCategory = byCategoryData;
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
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
                          "Statistiques Forums",
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 24),
                        isLoading
                            ? const Center(child: CircularProgressIndicator())
                            : Column(
                                children: [
                                  _buildOverviewCards(),
                                  const SizedBox(height: 32),
                                  _buildChartsSection(),
                                  const SizedBox(height: 32),
                                  _buildTopForumsSection(),
                                  const SizedBox(height: 32),
                                  _buildForumsByCategorySection(),
                                ],
                              ),
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
            children: [
              IconButton(
                icon: const Icon(Icons.menu),
                onPressed: () {},
              ),
              Row(
                children: const [
                  Icon(Icons.school, color: Colors.green, size: 32),
                  SizedBox(width: 10),
                  Text(
                    "naja7ni",
                    style: TextStyle(
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                      color: Colors.green,
                    ),
                  ),
                ],
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

  Widget _buildOverviewCards() {
    if (stats == null) return const SizedBox.shrink();

    return Row(
      children: [
        MetricCard(
          title: "Total Forums",
          value: stats!.totalForums.toString(),
          color: Colors.purple,
          icon: Icons.forum,
        ),
        const SizedBox(width: 16),
        MetricCard(
          title: "Forums Actifs",
          value: stats!.activeForums.toString(),
          color: Colors.green,
          icon: Icons.check_circle,
        ),
        const SizedBox(width: 16),
        MetricCard(
          title: "Forums Inactifs",
          value: stats!.inactiveForums.toString(),
          color: Colors.red,
          icon: Icons.cancel,
        ),
        const SizedBox(width: 16),
        MetricCard(
          title: "Total Messages",
          value: stats!.totalMessages.toString(),
          color: Colors.blue,
          icon: Icons.chat,
        ),
      ],
    );
  }

  Widget _buildChartsSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Analyse Visuelle",
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 16),
        Row(
          children: [
            Expanded(
              flex: 1,
              child: Container(
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
                      "Répartition Actif/Inactif",
                      style: TextStyle(
                          fontSize: 14, fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 16),
                    // Pie Chart simulé
                    SizedBox(
                      height: 200,
                      child: Stack(
                        children: [
                          Center(
                            child: Container(
                              width: 160,
                              height: 160,
                              child: CustomPaint(
                                painter: PieChartPainter(
                                  sections: [
                                    PieChartSection(
                                        value: stats?.activeForums ?? 0,
                                        color: Colors.green),
                                    PieChartSection(
                                        value: stats?.inactiveForums ?? 0,
                                        color: Colors.red),
                                  ],
                                ),
                              ),
                            ),
                          ),
                          Center(
                            child: Column(
                              mainAxisSize: MainAxisSize.min,
                              children: [
                                Icon(Icons.pie_chart,
                                    size: 40, color: Colors.grey[400]),
                                const SizedBox(height: 4),
                                Text(
                                  "${stats?.totalForums ?? 0}",
                                  style: const TextStyle(
                                      fontSize: 24, fontWeight: FontWeight.bold),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 16),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _buildLegendItem("Actifs", Colors.green,
                            stats?.activeForums ?? 0),
                        _buildLegendItem("Inactifs", Colors.red,
                            stats?.inactiveForums ?? 0),
                      ],
                    ),
                  ],
                ),
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              flex: 1,
              child: Container(
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
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          "Top Forums par Messages",
                          style: TextStyle(
                              fontSize: 14, fontWeight: FontWeight.w600),
                        ),
                        IconButton(
                          icon: const Icon(Icons.more_vert),
                          onPressed: () {},
                        ),
                      ],
                    ),
                    const SizedBox(height: 16),
                    _buildTopForumsChart(),
                  ],
                ),
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildLegendItem(String label, Color color, int value) {
    return Row(
      children: [
        Container(
          width: 12,
          height: 12,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
        const SizedBox(width: 6),
        Text("$label: $value", style: const TextStyle(fontSize: 12)),
      ],
    );
  }

  Widget _buildTopForumsChart() {
    if (topForums.isEmpty) {
      return const Center(
          child: Text("Aucune donnée disponible",
              style: TextStyle(color: Colors.grey)));
    }

    return Column(
      children: topForums.take(5).map((forum) {
        final title = forum['title']?.toString() ?? 'N/A';
        final count = forum['message_count']?.toString() ?? '0';
        final max = (topForums.first['message_count'] ?? 1) as num;

        return Padding(
          padding: const EdgeInsets.symmetric(vertical: 6),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Text(
                      title,
                      style: const TextStyle(fontSize: 12),
                      overflow: TextOverflow.ellipsis,
                    ),
                  ),
                  Text(
                    count,
                    style: const TextStyle(
                        fontSize: 12, fontWeight: FontWeight.bold),
                  ),
                ],
              ),
              const SizedBox(height: 4),
              ClipRRect(
                borderRadius: BorderRadius.circular(2),
                child: LinearProgressIndicator(
                  value: (forum['message_count'] as num?)?.toDouble() ?? 0.0 /
                      max.toDouble(),
                  backgroundColor: Colors.grey[200],
                  valueColor: AlwaysStoppedAnimation<Color>(
                    Colors.green.withOpacity(0.7),
                  ),
                  minHeight: 6,
                ),
              ),
            ],
          ),
        );
      }).toList(),
    );
  }

  Widget _buildTopForumsSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Top Forums par Messages",
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 16),
        Container(
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
          child: DataTableWidget(
            columns: ["Forum", "Messages", "Date création"],
            rows: topForums.map((forum) {
              final date =
                  DateTime.parse(forum['created_at'] ?? DateTime.now().toString());
              return [
                forum['title'] ?? 'N/A',
                forum['message_count']?.toString() ?? '0',
                "${date.day}/${date.month}/${date.year}",
              ];
            }).toList(),
          ),
        ),
      ],
    );
  }

  Widget _buildForumsByCategorySection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          "Forums par Catégorie",
          style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
        ),
        const SizedBox(height: 16),
        Container(
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
            children: forumsByCategory.map((data) {
              final category = data['category_title'] ?? 'N/A';
              final count = data['forum_count'] ?? 0;
              final max =
                  (forumsByCategory.isNotEmpty ? forumsByCategory.first['forum_count'] ?? 1 : 1) as num;

              return Padding(
                padding:
                    const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                child: Row(
                  children: [
                    Expanded(
                      flex: 2,
                      child: Text(
                        category,
                        style: const TextStyle(fontWeight: FontWeight.w500),
                      ),
                    ),
                    Expanded(
                      flex: 3,
                      child: LinearProgressIndicator(
                        value: (count as num).toDouble() / max.toDouble(),
                        backgroundColor: Colors.grey[200],
                        valueColor: AlwaysStoppedAnimation<Color>(
                          Colors.blue.withOpacity(0.7),
                        ),
                        minHeight: 8,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Text(
                      "$count forums",
                      style: const TextStyle(
                          fontWeight: FontWeight.bold, fontSize: 14),
                    ),
                  ],
                ),
              );
            }).toList(),
          ),
        ),
      ],
    );
  }

  void _handleNavigation(int index) {
    // TODO: Implémenter navigation
  }
}

// ==================== PIE CHART PAINTER ====================

class PieChartPainter extends CustomPainter {
  final List<PieChartSection> sections;

  PieChartPainter({required this.sections});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2;
    final startAngle = -pi / 2; // Start at top

    double currentAngle = startAngle;
    final total = sections.fold<double>(0, (sum, section) => sum + section.value);

    for (final section in sections) {
      final sweepAngle = (section.value / total) * 2 * pi;
      final paint = Paint()
        ..color = section.color
        ..style = PaintingStyle.fill;

      canvas.drawArc(
        Rect.fromCircle(center: center, radius: radius),
        currentAngle,
        sweepAngle,
        true,
        paint,
      );

      currentAngle += sweepAngle;
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => false;
}

class PieChartSection {
  final double value;
  final Color color;

  PieChartSection({required this.value, required this.color});
}
