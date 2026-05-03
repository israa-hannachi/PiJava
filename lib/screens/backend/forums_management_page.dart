// ============================================================================
// PAGE BACK-END : GESTION DES FORUMS (ADMIN)
// Liste, modification et suppression des forums
// ============================================================================

import 'package:flutter/material.dart';
import '../../../models/forum_models.dart';
import '../../../services/api_service.dart';
import '../widgets/admin_widgets.dart';
import 'create_forum_page.dart';

class ForumsManagementPage extends StatefulWidget {
  const ForumsManagementPage({Key? key}) : super(key: key);

  @override
  _ForumsManagementPageState createState() => _ForumsManagementPageState();
}

class _ForumsManagementPageState extends State<ForumsManagementPage> {
  late ApiService api;
  List<Forum> forums = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    api = ApiService();
    _loadForums();
  }

  Future<void> _loadForums() async {
    setState(() => isLoading = true);
    try {
      final data = await api.getForums();
      setState(() {
        forums = data;
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
    }
  }

  Future<void> _deleteForum(int id) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Confirmer la suppression"),
        content: const Text("Voulez-vous vraiment supprimer ce forum ?"),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text("Annuler"),
          ),
          ElevatedButton(
            onPressed: () => Navigator.pop(context, true),
            style: ElevatedButton.styleFrom(backgroundColor: Colors.red),
            child: const Text("Supprimer"),
          ),
        ],
      ),
    );

    if (confirm == true) {
      final success = await api.deleteForum(id);
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Forum supprimé"), backgroundColor: Colors.green),
        );
        _loadForums();
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF0F2F5),
      body: Row(
        children: [
          AdminSidebar(activeIndex: 4, onItemTapped: (index) {}),
          Expanded(
            child: Column(
              children: [
                _buildHeader(),
                Expanded(
                  child: isLoading
                      ? const Center(child: CircularProgressIndicator())
                      : _buildForumsList(),
                ),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => const CreateForumPage()),
        ).then((_) => _loadForums()),
        backgroundColor: Colors.green,
        child: const Icon(Icons.add),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 16),
      color: Colors.white,
      child: Row(
        children: const [
          Icon(Icons.forum, color: Colors.green, size: 32),
          SizedBox(width: 10),
          Text(
            "Gestion des Forums",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _buildForumsList() {
    if (forums.isEmpty) {
      return const Center(child: Text("Aucun forum trouvé."));
    }

    return ListView.builder(
      padding: const EdgeInsets.all(24),
      itemCount: forums.length,
      itemBuilder: (context, index) {
        final forum = forums[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: ListTile(
            title: Text(forum.title, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text(forum.description, maxLines: 1, overflow: TextOverflow.ellipsis),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.edit, color: Colors.blue),
                  onPressed: () => Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => CreateForumPage(forum: forum),
                    ),
                  ).then((_) => _loadForums()),
                ),
                IconButton(
                  icon: const Icon(Icons.delete, color: Colors.red),
                  onPressed: () => _deleteForum(forum.id!),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
