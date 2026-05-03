// ============================================================================
// PAGE BACK-END : CRÉER UN NOUVEAU FORUM (ADMIN)
// Formulaire de création avec validation
// ============================================================================

import 'package:flutter/material.dart';
import '../../models/forum_models.dart';
import '../../services/api_service.dart';
import 'widgets/admin_widgets.dart';

class CreateForumPage extends StatefulWidget {
  final Forum? forum;
  const CreateForumPage({Key? key, this.forum}) : super(key: key);

  @override
  _CreateForumPageState createState() => _CreateForumPageState();
}

class _CreateForumPageState extends State<CreateForumPage> {
  late ApiService api;

  // Contrôleurs de formulaire
  final TextEditingController _titleController = TextEditingController();
  final TextEditingController _descriptionController = TextEditingController();
  final TextEditingController _authorNameController = TextEditingController();

  int? selectedCategoryId;
  bool isActive = true;
  String? levelTag;

  List<Category> categories = [];
  bool isLoading = true;
  String? errorMessage;

  final List<String> levelTags = ['k1T', 'k2S', 'k3M', 'k4L', 'k5XL'];

  @override
  void initState() {
    super.initState();
    api = ApiService();
    if (widget.forum != null) {
      _titleController.text = widget.forum!.title;
      _descriptionController.text = widget.forum!.description;
      selectedCategoryId = widget.forum!.categoryId;
      isActive = widget.forum!.isActive;
      levelTag = widget.forum!.levelTag;
      _authorNameController.text = widget.forum!.author?.name ?? "";
    }
    _loadCategories();
  }

  Future<void> _loadCategories() async {
    try {
      final cats = await api.getCategories();
      setState(() {
        categories = cats;
        if (cats.isNotEmpty) {
          selectedCategoryId = cats.first.id;
        }
        isLoading = false;
      });
    } catch (e) {
      setState(() => isLoading = false);
    }
  }

  @override
  void dispose() {
    _titleController.dispose();
    _descriptionController.dispose();
    _authorNameController.dispose();
    super.dispose();
  }

  Future<void> _createForum() async {
    // Validation
    final title = _titleController.text.trim();
    final description = _descriptionController.text.trim();

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
    if (description.length < 10) {
      setState(() => errorMessage = "La description doit faire au moins 10 caractères");
      return;
    }
    if (selectedCategoryId == null) {
      setState(() => errorMessage = "Veuillez sélectionner une catégorie");
      return;
    }

    setState(() {
      errorMessage = null;
    });

    try {
      final forum = Forum(
        id: widget.forum?.id,
        title: _titleController.text.trim(),
        description: _descriptionController.text.trim(),
        categoryId: selectedCategoryId!,
        authorId: widget.forum?.authorId ?? 1,
        createdAt: widget.forum?.createdAt ?? DateTime.now(),
        isActive: isActive,
        levelTag: levelTag,
      );

      bool success = false;
      if (widget.forum == null) {
        final created = await api.createForum(forum);
        success = created != null;
      } else {
        success = await api.updateForum(forum);
      }

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(widget.forum == null ? "Forum créé avec succès" : "Forum modifié avec succès"),
            backgroundColor: Colors.green,
          ),
        );
        Navigator.pop(context, true); // Retour avec succès
      } else {
        setState(() => errorMessage = "Erreur lors de l'enregistrement");
      }
    } catch (e) {
      setState(() => errorMessage = "Erreur lors de la création");
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
                        // Breadcrumb
                        _buildBreadcrumb(),
                        const SizedBox(height: 24),

                        // Titre
                        Text(
                          widget.forum == null ? "Créer un nouveau forum" : "Modifier le forum",
                          style: const TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.bold,
                            color: Colors.black87,
                          ),
                        ),
                        const SizedBox(height: 24),

                        // Formulaire
                        _buildForm(),

                        const SizedBox(height: 32),

                        // Boutons d'action
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

  Widget _buildBreadcrumb() {
    return Row(
      children: [
        TextButton(
          onPressed: () => Navigator.pop(context),
          child: const Text(
            "Dashboard",
            style: TextStyle(color: Colors.grey, fontSize: 14),
          ),
        ),
        const Icon(Icons.chevron_right, color: Colors.grey, size: 16),
        Text(
          widget.forum == null ? "Créer Forum" : "Modifier Forum",
          style: const TextStyle(
            color: Colors.green,
            fontSize: 14,
            fontWeight: FontWeight.bold,
          ),
        ),
      ],
    );
  }

  Widget _buildForm() {
    if (isLoading) {
      return const Center(child: CircularProgressIndicator());
    }

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
          // Titre du forum
          _buildTextField(
            controller: _titleController,
            label: "Titre du forum *",
            hint: "Entrez le titre du forum",
            icon: Icons.title,
          ),
          const SizedBox(height: 20),

          // Catégorie
          _buildCategoryDropdown(),
          const SizedBox(height: 20),

          // Description détaillée
          _buildTextField(
            controller: _descriptionController,
            label: "Description détaillée *",
            hint: "Décrivez le sujet du forum",
            icon: Icons.description,
            maxLines: 4,
          ),
          const SizedBox(height: 20),

          // Nom du créateur
          _buildTextField(
            controller: _authorNameController,
            label: "Nom du créateur (optionnel)",
            hint: "Nom de l'enseignant ou responsable",
            icon: Icons.person,
          ),
          const SizedBox(height: 20),

          // État de publication
          _buildToggleSwitch(
            label: "État de publication",
            value: isActive,
            onChanged: (val) => setState(() => isActive = val),
          ),
          const SizedBox(height: 20),

          // Tag de niveau
          _buildLevelTagDropdown(),
          const SizedBox(height: 20),

          // Message d'erreur
          if (errorMessage != null)
            Padding(
              padding: const EdgeInsets.only(top: 8),
              child: Text(
                errorMessage!,
                style: const TextStyle(color: Colors.red, fontSize: 12),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildTextField({
    required TextEditingController controller,
    required String label,
    required String hint,
    required IconData icon,
    int maxLines = 1,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(icon, size: 18, color: Colors.grey[600]),
            const SizedBox(width: 8),
            Text(
              label,
              style: const TextStyle(
                fontWeight: FontWeight.w600,
                fontSize: 14,
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
        TextField(
          controller: controller,
          maxLines: maxLines,
          decoration: InputDecoration(
            hintText: hint,
            border: const OutlineInputBorder(),
            contentPadding: const EdgeInsets.all(12),
          ),
        ),
      ],
    );
  }

  Widget _buildCategoryDropdown() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.category, size: 18, color: Colors.grey[600]),
            const SizedBox(width: 8),
            const Text(
              "Catégorie *",
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
          ],
        ),
        const SizedBox(height: 8),
        DropdownButtonFormField<int>(
          value: selectedCategoryId,
          decoration: const InputDecoration(
            border: OutlineInputBorder(),
            contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
          ),
          items: categories
              .map((cat) => DropdownMenuItem(
                    value: cat.id,
                    child: Text(cat.title),
                  ))
              .toList(),
          onChanged: (value) {
            setState(() => selectedCategoryId = value);
          },
        ),
      ],
    );
  }

  Widget _buildLevelTagDropdown() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.label, size: 18, color: Colors.grey[600]),
            const SizedBox(width: 8),
            const Text(
              "Tag de niveau (optionnel)",
              style: TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
          ],
        ),
        const SizedBox(height: 8),
        DropdownButtonFormField<String>(
          value: levelTag,
          decoration: const InputDecoration(
            border: OutlineInputBorder(),
            contentPadding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            hintText: "Sélectionner un niveau",
          ),
          items: levelTags
              .map((tag) => DropdownMenuItem(
                    value: tag,
                    child: Text(tag),
                  ))
              .toList(),
          onChanged: (value) {
            setState(() => levelTag = value);
          },
        ),
      ],
    );
  }

  Widget _buildToggleSwitch({
    required String label,
    required bool value,
    required Function(bool) onChanged,
  }) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Row(
          children: [
            Icon(Icons.toggle_on, size: 18, color: Colors.grey[600]),
            const SizedBox(width: 8),
            Text(
              label,
              style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 14),
            ),
          ],
        ),
        Switch(
          value: value,
          onChanged: onChanged,
          activeColor: Colors.green,
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
        TextButton(
          onPressed: () {
            _titleController.clear();
            _descriptionController.clear();
            _authorNameController.clear();
            setState(() {
              errorMessage = null;
              isActive = true;
              levelTag = null;
            });
          },
          child: const Text("Annuler"),
        ),
        const SizedBox(width: 12),
        ElevatedButton.icon(
          onPressed: _createForum,
          icon: Icon(widget.forum == null ? Icons.add : Icons.save, size: 18),
          label: Text(widget.forum == null ? "Créer le Forum" : "Enregistrer"),
          style: ElevatedButton.styleFrom(
            backgroundColor: Colors.green,
            foregroundColor: Colors.white,
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
          ),
        ),
      ],
    );
  }

  void _handleNavigation(int index) {
    // TODO: Implémenter la navigation admin
  }
}
