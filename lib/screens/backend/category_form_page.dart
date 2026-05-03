// ============================================================================
// PAGE BACK-END : FORMULAIRE CATÉGORIE (ADMIN)
// Ajouter/Modifier une catégorie
// ============================================================================

import 'package:flutter/material.dart';
import '../../models/forum_models.dart';
import '../../services/api_service.dart';
import 'widgets/admin_widgets.dart';
import 'admin_dashboard.dart';

class CategoryFormPage extends StatefulWidget {
  final Category? category; // Si null = création, sinon modification

  const CategoryFormPage({Key? key, this.category}) : super(key: key);

  @override
  _CategoryFormPageState createState() => _CategoryFormPageState();
}

class _CategoryFormPageState extends State<CategoryFormPage> {
  late ApiService api;

  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _descriptionController = TextEditingController();
  final TextEditingController _iconUrlController = TextEditingController();

  final List<Color> availableColors = [
    Colors.green,
    Colors.blue,
    Colors.orange,
    Colors.red,
    Colors.purple,
    Colors.teal,
    Colors.indigo,
   Colors.pink,
  ];

  Color selectedColor = Colors.green;
  bool isLoading = false;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    api = ApiService();

    // Si modification, pré-remplir
    if (widget.category != null) {
      _titleController.text = widget.category!.title;
      _descriptionController.text = widget.category!.description;
      _iconUrlController.text = widget.category!.iconUrl;
      selectedColor = widget.category!.color;
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _iconUrlController.dispose();
    super.dispose();
  }

  Future<void> _saveCategory() async {
    final title = _titleController.text.trim();
    final description = _descriptionController.text.trim();
    final iconUrl = _iconUrlController.text.trim();

    if (title.isEmpty) {
      setState(() => errorMessage = "Le titre est requis");
      return;
    }
    if (title.length < 3) {
      setState(() => errorMessage = "Le titre doit faire au moins 3 caractères");
      return;
    }
    if (description.isEmpty) {
      setState(() => errorMessage = "La description est requise");
      return;
    }
    if (iconUrl.isNotEmpty && !Uri.parse(iconUrl).isAbsolute) {
      setState(() => errorMessage = "L'URL de l'icône n'est pas valide");
      return;
    }

    setState(() => isLoading = true);

    try {
      final category = Category(
        id: widget.category?.id,
        title: _titleController.text.trim(),
        description: _descriptionController.text.trim(),
        iconUrl: _iconUrlController.text.trim(),
        color: selectedColor,
      );

      bool success;
      if (widget.category == null) {
        success = await api.createCategory(category);
      } else {
        success = await api.updateCategory(category);
      }

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(widget.category == null
                ? "Catégorie créée avec succès"
                : "Catégorie modifiée avec succès"),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context, true);
      } else {
        setState(() => errorMessage = "Erreur lors de l'enregistrement");
      }
    } catch (e) {
      setState(() => errorMessage = "Erreur: ${e.toString()}");
    } finally {
      setState(() => isLoading = false);
    }
  }

  Future<void> _deleteCategory() async {
    if (widget.category == null) return;

    final confirm = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text("Confirmer la suppression"),
        content: const Text(
            "Êtes-vous sûr de vouloir supprimer cette catégorie ? Cette action est irréversible."),
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
      final success = await api.deleteCategory(widget.category!.id!);
      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text("Catégorie supprimée"),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context, true);
      } else {
        _showErrorSnackBar("Erreur lors de la suppression");
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
            activeIndex: 4,
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
                        _buildBreadcrumb(),
                        const SizedBox(height: 24),
                        Text(
                          widget.category == null
                              ? "Ajouter une catégorie"
                              : "Modifier la catégorie",
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 24),
                        _buildForm(),
                        const SizedBox(height: 32),
                        _buildActionButtons(),
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

  Widget _buildBreadcrumb() {
    return Row(
      children: [
        TextButton(
          onPressed: () => Navigator.pushReplacement(
            context,
            MaterialPageRoute(
                builder: (context) => const AdminDashboardPage()),
          ),
          child: const Text("Dashboard", style: TextStyle(color: Colors.grey)),
        ),
        const Icon(Icons.chevron_right, color: Colors.grey, size: 16),
        TextButton(
          onPressed: () {},
          child: const Text("Forums", style: TextStyle(color: Colors.grey)),
        ),
        const Icon(Icons.chevron_right, color: Colors.grey, size: 16),
        Text(
          widget.category == null ? "Ajouter" : "Modifier",
          style: const TextStyle(color: Colors.green, fontWeight: FontWeight.bold),
        ),
      ],
    );
  }

  Widget _buildForm() {
    return Container(
      padding: const EdgeInsets.all(24),
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
          _buildTextField(
            controller: _titleController,
            label: "Titre de la catégorie *",
            icon: Icons.label,
          ),
          const SizedBox(height: 20),
          _buildTextField(
            controller: _descriptionController,
            label: "Description détaillée *",
            icon: Icons.description,
            maxLines: 4,
          ),
          const SizedBox(height: 20),
          _buildTextField(
            controller: _iconUrlController,
            label: "URL de l'icône (optionnel)",
            icon: Icons.image,
            hint: "https://exemple.com/icon.png",
          ),
          const SizedBox(height: 20),
          _buildColorPicker(),
          if (errorMessage != null) ...[
            const SizedBox(height: 12),
            Text(errorMessage!,
                style: const TextStyle(color: Colors.red, fontSize: 12)),
          ],
        ],
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required IconData icon,
    int maxLines = 1,
    String? hint,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(icon, size: 18, color: Colors.grey[600]),
            const SizedBox(width: 8),
            Text(label,
                style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
          ],
        ),
        const SizedBox(height: 8),
        TextField(
          controller: controller,
          maxLines: maxLines,
          decoration: InputDecoration(
            hintText: hint,
            border: const OutlineInputBorder(),
            contentPadding:
                const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
          ),
        ),
      ],
    );
  }

  Widget _buildColorPicker() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.palette, size: 18, color: Colors.grey[600]),
            const SizedBox(width: 8),
            const Text("Couleur de l'icône",
                style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14)),
          ],
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 12,
          children: availableColors.map((color) {
            return GestureDetector(
              onTap: () => setState(() => selectedColor = color),
              child: Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: color,
                  shape: BoxShape.circle,
                  border: Border.all(
                    color: selectedColor == color ? Colors.black : Colors.grey,
                    width: selectedColor == color ? 3 : 1,
                  ),
                ),
              ),
            );
          }).toList(),
        ),
      ],
    );
  }

  Widget _buildActionButtons() {
    return Row(
      children: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text("Retour"),
        ),
        const Spacer(),
        if (widget.category != null)
          TextButton(
            onPressed: _deleteCategory,
            style: TextButton.styleFrom(foregroundColor: Colors.red),
            child: const Text("Effacer"),
          ),
        const SizedBox(width: 12),
        ElevatedButton.icon(
          onPressed: isLoading ? null : _saveCategory,
          icon: isLoading
              ? const SizedBox(
                  width: 16,
                  height: 16,
                  child: CircularProgressIndicator(
                    strokeWidth: 2,
                    color: Colors.white,
                  ),
                )
              : Icon(widget.category == null ? Icons.add : Icons.save),
          label:
              Text(isLoading ? "Enregistrement..." : (widget.category == null ? "Ajouter" : "Enregistrer")),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.orange,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          ),
        ),
      ],
    );
  }

  void _handleNavigation(int index) {
    // TODO: Implémenter navigation admin
  }
}
