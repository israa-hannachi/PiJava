// ============================================================================
// WIDGETS COMMUNS - Éléments UI réutilisables
// Sidebar navigation, Header, Cards, etc.
// ============================================================================

import 'package:flutter/material.dart';
import '../models/forum_models.dart';
import '../services/auth_service.dart';

// ==================== SIDEBAR FRONT-OFFICE ====================

class FrontSidebar extends StatelessWidget {
  final int activeIndex;
  final List<NavigationItem> items;
  final Function(int) onItemTapped;

  const FrontSidebar({
    Key? key,
    required this.activeIndex,
    required this.items,
    required this.onItemTapped,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 70,
      color: Colors.white,
      child: Column(
        children: [
          const SizedBox(height: 20),
          // Logo
          Container(
            width: 40,
            height: 40,
            decoration: const BoxDecoration(
              color: Colors.green,
              shape: BoxShape.circle,
            ),
            child: const Center(
              child: Text(
                "N",
                style: TextStyle(
                  color: Colors.white,
                  fontWeight: FontWeight.bold,
                  fontSize: 20,
                ),
              ),
            ),
          ),
          const SizedBox(height: 30),
          // Icônes navigation
          ...items.asMap().entries.map((entry) {
            return _buildNavIcon(
              entry.value.icon,
              entry.key == activeIndex,
              () => onItemTapped(entry.key),
            );
          }).toList(),
          const Spacer(),
          // Déconnexion
          IconButton(
            icon: const Icon(Icons.logout, color: Colors.grey),
            onPressed: () async {
              await AuthService().logout();
              //Navigator.pushReplacementNamed(context, '/login');
            },
          ),
          const SizedBox(height: 20),
        ],
      ),
    );
  }

  Widget _buildNavIcon(IconData icon, bool isActive, VoidCallback onTap) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 8),
        child: Icon(
          icon,
          color: isActive ? Colors.green : Colors.grey,
          size: 28,
        ),
      ),
    );
  }
}

class NavigationItem {
  final IconData icon;
  final String label;

  NavigationItem({required this.icon, required this.label});
}

// ==================== SIDEBAR BACK-OFFICE (ADMIN) ====================

class AdminSidebar extends StatelessWidget {
  final int activeIndex;
  final Function(int) onItemTapped;

  const AdminSidebar({
    Key? key,
    required this.activeIndex,
    required this.onItemTapped,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final items = [
      {'icon': Icons.dashboard, 'label': 'Dashboard'},
      {'icon': Icons.people, 'label': 'Comptes'},
      {'icon': Icons.book, 'label': 'Cours'},
      {'icon': Icons.sports_esports, 'label': 'Jeux'},
      {'icon': Icons.forum, 'label': 'Forums', 'submenu': true},
      {'icon': Icons.event, 'label': 'Events'},
      {'icon': Icons.video_call, 'label': 'Meet'},
      {'icon': Icons.mail, 'label': 'Mailing'},
      {'icon': Icons.arrow_back, 'label': 'Retour Front'},
      {'icon': Icons.logout, 'label': 'Logout'},
    ];

    return Container(
      width: 250,
      color: Colors.black,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const SizedBox(height: 20),
          // Logo admin
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 16),
            child: Row(
              children: const [
                Icon(Icons.admin_panel_settings, color: Colors.red, size: 28),
                SizedBox(width: 10),
                Text(
                  "ADMIN",
                  style: TextStyle(
                    color: Colors.white,
                    fontWeight: FontWeight.bold,
                    fontSize: 16,
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(height: 30),
          // Menu items
          ...items.asMap().entries.map((entry) {
            final item = entry.value;
            final isActive = entry.key == activeIndex;
            final hasSubmenu = item['submenu'] == true;

            return Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                if (hasSubmenu)
                  Padding(
                    padding: const EdgeInsets.only(left: 12, top: 8, bottom: 8),
                    child: Text(
                      "FORUMS",
                      style: TextStyle(
                        fontSize: 10,
                        color: Colors.grey[400],
                        fontWeight: FontWeight.w600,
                        letterSpacing: 1.2,
                      ),
                    ),
                  ),
                _buildMenuItem(
                  icon: item['icon'] as IconData,
                  label: item['label'] as String,
                  isActive: isActive,
                  hasSubmenu: hasSubmenu,
                  onTap: () => onItemTapped(entry.key),
                ),
              ],
            );
          }).toList(),
          const Spacer(),
          // Version
          Padding(
            padding: const EdgeInsets.all(16),
            child: Text(
              "v1.0.0",
              style: TextStyle(
                fontSize: 10,
                color: Colors.grey[600],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildMenuItem({
    required IconData icon,
    required String label,
    required bool isActive,
    bool hasSubmenu = false,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        margin: EdgeInsets.only(
          left: hasSubmenu ? 0 : 12,
          right: 12,
          top: 4,
          bottom: 4,
        ),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
        decoration: BoxDecoration(
          color: isActive ? Colors.green.withOpacity(0.2) : Colors.transparent,
          borderRadius: BorderRadius.circular(6),
        ),
        child: Row(
          children: [
            Icon(
              icon,
              size: 20,
              color: isActive ? Colors.green : Colors.grey[400],
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                label,
                style: TextStyle(
                  fontSize: 13,
                  color: isActive ? Colors.green : Colors.grey[300],
                  fontWeight: isActive ? FontWeight.w600 : FontWeight.normal,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ==================== HEADER COMMUN ====================

class CommonHeader extends StatelessWidget {
  final String title;
  final List<Widget>? actions;
  final bool showBack;
  final VoidCallback? onBack;

  const CommonHeader({
    Key? key,
    required this.title,
    this.actions,
    this.showBack = false,
    this.onBack,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      color: Colors.white,
      child: Row(
        children: [
          if (showBack)
            IconButton(
              icon: const Icon(Icons.arrow_back),
              onPressed: onBack ?? () => Navigator.pop(context),
            )
          else
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
          const Spacer(),
          if (actions != null) ...?actions,
        ],
      ),
    );
  }
}

// ==================== CARTES MÉTRIQUES (DASHBOARD) ====================

class MetricCard extends StatelessWidget {
  final String title;
  final String value;
  final Color color;
  final IconData icon;

  const MetricCard({
    Key? key,
    required this.title,
    required this.value,
    required this.color,
    required this.icon,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Expanded(
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
                Icon(icon, color: color, size: 28),
                Container(
                  padding: const EdgeInsets.all(6),
                  decoration: BoxDecoration(
                    color: color.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(6),
                  ),
                  child: Text(
                    value,
                    style: TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                      color: color,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              title,
              style: const TextStyle(
                fontSize: 14,
                color: Colors.grey,
                fontWeight: FontWeight.w500,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// ==================== BOUTONS D'ACTION ====================

class ActionButton extends StatelessWidget {
  final String label;
  final IconData icon;
  final Color color;
  final bool isOutlined;
  final VoidCallback? onPressed;

  const ActionButton({
    Key? key,
    required this.label,
    required this.icon,
    required this.color,
    this.isOutlined = false,
    this.onPressed,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: isOutlined
          ? OutlinedButton.icon(
              onPressed: onPressed,
              icon: Icon(icon, size: 18),
              label: Text(label),
              style: OutlinedButton.styleFrom(
                foregroundColor: color,
                side: BorderSide(color: color),
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            )
          : ElevatedButton.icon(
              onPressed: onPressed,
              icon: Icon(icon, size: 18),
              label: Text(label),
              style: ElevatedButton.styleFrom(
                backgroundColor: color,
                foregroundColor: Colors.white,
                padding: const EdgeInsets.symmetric(vertical: 12),
              ),
            ),
    );
  }
}

// ==================== BREADCRUMB ====================

class Breadcrumb extends StatelessWidget {
  final List<String> segments;

  const Breadcrumb({Key? key, required this.segments}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        ...segments.asMap().entries.map((entry) {
          final isLast = entry.key == segments.length - 1;
          return Row(
            children: [
              if (entry.key > 0)
                const Icon(Icons.chevron_right, color: Colors.grey, size: 16),
              TextButton(
                onPressed: isLast
                    ? null
                    : () {
                        // Navigation selon le segment
                      },
                child: Text(
                  entry.value,
                  style: TextStyle(
                    fontSize: 14,
                    color: isLast ? Colors.green : Colors.grey,
                    fontWeight: isLast ? FontWeight.bold : FontWeight.normal,
                  ),
                ),
              ),
            ],
          );
        }).toList(),
      ],
    );
  }
}

// ==================== TABLEAU DE DONNÉES ====================

class DataTableWidget extends StatelessWidget {
  final List<String> columns;
  final List<List<String>> rows;
  final List<Widget>? actions;

  const DataTableWidget({
    Key? key,
    required this.columns,
    required this.rows,
    this.actions,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
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
        children: [
          // En-tête
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.grey[100],
              borderRadius: const BorderRadius.only(
                topLeft: Radius.circular(12),
                topRight: Radius.circular(12),
              ),
            ),
            child: Row(
              children: columns
                  .map((col) => Expanded(
                        flex: col.length > 15 ? 2 : 1,
                        child: Text(
                          col,
                          style: const TextStyle(
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                        ),
                      ))
                  .toList(),
            ),
          ),
          // Lignes
          ...rows.map((row) {
            return Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                border: Border(
                  bottom: BorderSide(color: Colors.grey.shade200),
                ),
              ),
              child: Row(
                children: row
                    .map((cell) => Expanded(
                          flex: cell.length > 15 ? 2 : 1,
                          child: Text(
                            cell,
                            style: const TextStyle(fontSize: 13),
                            overflow: TextOverflow.ellipsis,
                          ),
                        ))
                    .toList(),
              ),
            );
          }).toList(),
          // Actions
          if (actions != null)
            Container(
              padding: const EdgeInsets.all(12),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.end,
                children: actions!,
              ),
            ),
        ],
      ),
    );
  }
}

// ==================== CLUSTER CARD ====================

class ClusterCard extends StatelessWidget {
  final Cluster cluster;

  const ClusterCard({Key? key, required this.cluster}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            blurRadius: 6,
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
              Expanded(
                child: Text(
                  cluster.name,
                  style: const TextStyle(
                    fontSize: 16,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.purple.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(6),
                ),
                child: Text(
                  "${cluster.forumCount} forums",
                  style: const TextStyle(
                    fontSize: 11,
                    color: Colors.purple,
                    fontWeight: FontWeight.w500,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          // Tags
          Wrap(
            spacing: 6,
            runSpacing: 6,
            children: cluster.tags.map((tag) {
              return Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.blue.withOpacity(0.1),
                  borderRadius: BorderRadius.circular(4),
                ),
                child: Text(
                  tag,
                  style: const TextStyle(
                    fontSize: 10,
                    color: Colors.blue,
                  ),
                ),
              );
            }).toList(),
          ),
          const SizedBox(height: 12),
          // Description
          Text(
            cluster.description,
            style: TextStyle(
              fontSize: 12,
              color: Colors.grey[600],
              height: 1.4,
            ),
            maxLines: 2,
            overflow: TextOverflow.ellipsis,
          ),
          const SizedBox(height: 12),
          // Activité des thèmes
          ...cluster.themes.map((theme) {
            return Padding(
              padding: const EdgeInsets.only(bottom: 6),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Text(
                        theme.name,
                        style: const TextStyle(
                          fontSize: 11,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                      Text(
                        "${(theme.activityLevel * 100).toInt()}%",
                        style: TextStyle(
                          fontSize: 10,
                          color: Colors.grey[500],
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 2),
                  ClipRRect(
                    borderRadius: BorderRadius.circular(2),
                    child: LinearProgressIndicator(
                      value: theme.activityLevel,
                      backgroundColor: Colors.grey[200],
                      valueColor: AlwaysStoppedAnimation<Color>(
                        theme.activityLevel > 0.7
                            ? Colors.green
                            : theme.activityLevel > 0.3
                                ? Colors.orange
                                : Colors.red,
                      ),
                      minHeight: 4,
                    ),
                  ),
                ],
              ),
            );
          }).toList(),
        ],
      ),
    );
  }
}

// ==================== EXPORT MODAL ====================

class ExportModal extends StatefulWidget {
  const ExportModal({Key? key}) : super(key: key);

  @override
  _ExportModalState createState() => _ExportModalState();
}

class _ExportModalState extends State<ExportModal> {
  final TextEditingController _filenameController =
      TextEditingController(text: 'rapport_forums');

  @override
  Widget build(BuildContext context) {
    return AlertDialog(
      title: const Text("Exporter le Rapport"),
      content: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          TextField(
            controller: _filenameController,
            decoration: const InputDecoration(
              labelText: "Nom du fichier",
              hintText: "rapport_forums",
              border: OutlineInputBorder(),
            ),
          ),
          const SizedBox(height: 20),
          const Text(
            "Choisir le format d'export :",
            style: TextStyle(fontWeight: FontWeight.w500),
          ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: [
              _buildExportButton("PDF", Colors.red, Icons.picture_as_pdf),
              _buildExportButton("Word", Colors.blue, Icons.description),
              _buildExportButton("Excel", Colors.green, Icons.table_chart),
              _buildExportButton("CSV", Colors.green.shade800, Icons.csv),
            ],
          ),
        ],
      ),
      actions: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text("Annuler"),
        ),
      ],
    );
  }

  Widget _buildExportButton(String format, Color color, IconData icon) {
    return Column(
      children: [
        GestureDetector(
          onTap: () {
            // TODO: Implémenter l'export
            Navigator.pop(context);
            _showExportSuccess(format);
          },
          child: Container(
            width: 50,
            height: 50,
            decoration: BoxDecoration(
              color: color.withOpacity(0.1),
              borderRadius: BorderRadius.circular(8),
            ),
            child: Icon(icon, color: color, size: 28),
          ),
        ),
        const SizedBox(height: 4),
        Text(
          format,
          style: const TextStyle(fontSize: 11),
        ),
      ],
    );
  }

  void _showExportSuccess(String format) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text("Export $format réussi"),
        backgroundColor: Colors.green,
      ),
    );
  }
}
