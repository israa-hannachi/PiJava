// ============================================================================
// PAGE BACK-END : GESTION DES CATÉGORIES (ADMIN)
// Liste, modification et suppression des catégories
// ============================================================================

import 'package:flutter/material.dart';
import '../../models/forum_models.dart';
import '../../services/api_service.dart';
import 'widgets/admin_widgets.dart';
import 'category_form_page.dart';

class CategoriesManagementPage extends StatefulWidget {
  const CategoriesManagementPage({Key? key}) : super(key: key);

  @override
  _CategoriesManagementPageState createState() => _CategoriesManagementPageState();
}

class _CategoriesManagementPageState extends State<CategoriesManagementPage> {
  late ApiService api;
  List<Category> categories = [];
  bool isLoading = true;

  @override
  void initState() {
    super.initState();
    api = ApiService();
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    setState(() => isLoading = true);
    try {
      final data = await api.getCategories();
      setState(() {
        categories = data;
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
    }
  }

  Future<void> _deleteCategory(int id) async {
    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Confirmer la suppression"),
        content: const Text("Voulez-vous vraiment supprimer cette catégorie ?"),
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
      final success = await api.deleteCategory(id);
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text("Catégorie supprimée"), backgroundColor: Colors.green),
        );
        _loadCategories();
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
                      : _buildCategoriesList(),
                ),
              ],
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () => Navigator.push(
          context,
          MaterialPageRoute(builder: (context) => const CategoryFormPage()),
        ).then((_) => _loadCategories()),
        backgroundColor: Colors.blue,
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
          Icon(Icons.category, color: Colors.blue, size: 32),
          SizedBox(width: 10),
          Text(
            "Gestion des Catégories",
            style: TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
        ],
      ),
    );
  }

  Widget _buildCategoriesList() {
    if (categories.isEmpty) {
      return const Center(child: Text("Aucune catégorie trouvée."));
    }

    return ListView.builder(
      padding: const EdgeInsets.all(24),
      itemCount: categories.length,
      itemBuilder: (context, index) {
        final category = categories[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: ListTile(
            leading: Icon(Icons.folder, color: category.color),
            title: Text(category.title, style: const TextStyle(fontWeight: FontWeight.bold)),
            subtitle: Text(category.description, maxLines: 1, overflow: TextOverflow.ellipsis),
            trailing: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                IconButton(
                  icon: const Icon(Icons.edit, color: Colors.blue),
                  onPressed: () => Navigator.push(
                    context,
                    MaterialPageRoute(
                      builder: (context) => CategoryFormPage(category: category),
                    ),
                  ).then((_) => _loadCategories()),
                ),
                IconButton(
                  icon: const Icon(Icons.delete, color: Colors.red),
                  onPressed: () => _deleteCategory(category.id!),
                ),
              ],
            ),
          ),
        );
      },
    );
  }
}
