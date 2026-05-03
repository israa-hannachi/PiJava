// ============================================================================
// PAGE AUTHENTIFICATION - Connexion utilisateur
// ============================================================================

import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../services/auth_service.dart';
import '../models/forum_models.dart';
import 'admin_dashboard.dart';
import 'frontend/forums_page.dart';

class LoginPage extends StatefulWidget {
  const LoginPage({Key? key}) : super(key: key);

  @override
  _LoginPageState createState() => _LoginPageState();
}

class _LoginPageState extends State<LoginPage> {
  final _formKey = GlobalKey<FormState>();
  final _emailController = TextEditingController();
  final _passwordController = TextEditingController();

  bool _isLoading = false;
  String? _errorMessage;
  bool _isLogin = true;

  @override
  void dispose() {
    _emailController.dispose();
    _passwordController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isLoading = true;
      _errorMessage = null;
    });

    try {
      final auth = AuthService();
      bool success;

      if (_isLogin) {
        success = await auth.login(
          _emailController.text.trim(),
          _passwordController.text,
        );
      } else {
        success = await auth.register(
          _emailController.text.trim(),
          _emailController.text.trim(), // Nom = email pour simplifier
          _passwordController.text,
        );
      }

      if (success && mounted) {
        final user = await auth.getCurrentUser();
        if (user != null) {
          // Redirection selon le rôle
          if (user.isAdmin) {
            Navigator.pushReplacementNamed(context, '/admin');
          } else {
            Navigator.pushReplacementNamed(context, '/forums');
          }
        }
      } else {
        setState(() {
          _errorMessage = _isLogin
              ? "Email ou mot de passe incorrect"
              : "Erreur lors de l'inscription";
        });
      }
    } catch (e) {
      setState(() {
        _errorMessage = "Erreur de connexion au serveur";
      });
    } finally {
      setState(() => _isLoading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.white,
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(32),
          child: Form(
            key: _formKey,
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                // Logo
                Container(
                  width: 100,
                  height: 100,
                  decoration: const BoxDecoration(
                    color: Colors.green,
                    shape: BoxShape.circle,
                  ),
                  child: const Center(
                    child: Text(
                      "N",
                      style: TextStyle(
                        color: Colors.white,
                        fontSize: 50,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
                const SizedBox(height: 24),
                const Text(
                  "naja7ni",
                  style: TextStyle(
                    fontSize: 32,
                    fontWeight: FontWeight.bold,
                    color: Colors.green,
                  ),
                ),
                const SizedBox(height: 8),
                Text(
                  _isLogin ? "Connectez-vous" : "Créez un compte",
                  style: TextStyle(
                    fontSize: 16,
                    color: Colors.grey[600],
                  ),
                ),
                const SizedBox(height: 48),

                // Formulaire
                if (_errorMessage != null)
                  Container(
                    padding: const EdgeInsets.all(12),
                    margin: const EdgeInsets.only(bottom: 24),
                    decoration: BoxDecoration(
                      color: Colors.red.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Row(
                      children: [
                        const Icon(Icons.error, color: Colors.red, size: 20),
                        const SizedBox(width: 8),
                        Expanded(
                          child: Text(
                            _errorMessage!,
                            style: const TextStyle(color: Colors.red),
                          ),
                        ),
                      ],
                    ),
                  ),

                // Email
                TextFormField(
                  controller: _emailController,
                  keyboardType: TextInputType.emailAddress,
                  decoration: const InputDecoration(
                    labelText: "Email",
                    prefixIcon: Icon(Icons.email),
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return "Veuillez entrer votre email";
                    }
                    if (!value.contains('@')) {
                      return "Email invalide";
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 20),

                // Mot de passe
                TextFormField(
                  controller: _passwordController,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: "Mot de passe",
                    prefixIcon: Icon(Icons.lock),
                    border: OutlineInputBorder(),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return "Veuillez entrer votre mot de passe";
                    }
                    if (value.length < 6) {
                      return "Minimum 6 caractères";
                    }
                    return null;
                  },
                ),
                const SizedBox(height: 32),

                // Bouton connexion/inscription
                SizedBox(
                  width: double.infinity,
                  height: 50,
                  child: ElevatedButton(
                    onPressed: _isLoading ? null : _submit,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: Colors.green,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(8),
                      ),
                    ),
                    child: _isLoading
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(
                              strokeWidth: 2,
                              valueColor:
                                  AlwaysStoppedAnimation<Color>(Colors.white),
                            ),
                          )
                        : Text(
                            _isLogin ? "Se connecter" : "S'inscrire",
                            style: const TextStyle(
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                  ),
                ),
                const SizedBox(height: 24),

                // Lien basculement login/register
                TextButton(
                  onPressed: () {
                    setState(() {
                      _isLogin = !_isLogin;
                      _errorMessage = null;
                    });
                  },
                  child: Text(
                    _isLogin
                        ? "Pas de compte ? Inscrivez-vous"
                        : "Déjà un compte ? Connectez-vous",
                    style: const TextStyle(fontSize: 14),
                  ),
                ),

                // Comptes de test
                const SizedBox(height: 32),
                const Divider(),
                const SizedBox(height: 16),
                const Text(
                  "Comptes de test",
                  style: TextStyle(
                    fontWeight: FontWeight.bold,
                    color: Colors.grey,
                  ),
                ),
                const SizedBox(height: 8),
                _buildTestAccount(
                  "Utilisateur standard",
                  "user@test.com",
                  "password",
                  false,
                ),
                const SizedBox(height: 8),
                _buildTestAccount(
                  "Administrateur",
                  "admin@test.com",
                  "password",
                  true,
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildTestAccount(String role, String email, String password, bool isAdmin) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: isAdmin ? Colors.red.withOpacity(0.05) : Colors.green.withOpacity(0.05),
        borderRadius: BorderRadius.circular(8),
        border: Border.all(
          color: isAdmin ? Colors.red.withOpacity(0.2) : Colors.green.withOpacity(0.2),
        ),
      ),
      child: Row(
        children: [
          Icon(
            isAdmin ? Icons.admin_panel_settings : Icons.person,
            color: isAdmin ? Colors.red : Colors.green,
            size: 16,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  role,
                  style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 12),
                ),
                Text(
                  email,
                  style: const TextStyle(fontSize: 11, color: Colors.grey),
                ),
              ],
            ),
          ),
          TextButton(
            onPressed: () {
              _emailController.text = email;
              _passwordController.text = password;
            },
            child: const Text("Remplir", style: TextStyle(fontSize: 11)),
          ),
        ],
      ),
    );
  }
}
