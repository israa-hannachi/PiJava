// ============================================================================
// WIDGETS ADMIN - Composants UI pour interface back-office
// Sidebar admin, cards, tables, etc.
// ============================================================================

import 'package:flutter/material.dart';

// ==================== SIDEBAR ADMIN ====================

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

// ==================== HEADER ADMIN ====================

class AdminHeader extends StatelessWidget {
  const AdminHeader({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
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
              const SizedBox(width: 8),
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
                child: const Text(
                  "Dashboard",
                  style: TextStyle(color: Colors.black87),
                ),
              ),
              const SizedBox(width: 8),
              ElevatedButton.icon(
                onPressed: () async {
                  await AuthService().logout();
                },
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
}

// ==================== METRIC CARD ====================

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

// ==================== ACTION BUTTON ====================

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

// ==================== DATA TABLE ====================

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
                border:
                    Border(bottom: BorderSide(color: Colors.grey.shade200)),
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
